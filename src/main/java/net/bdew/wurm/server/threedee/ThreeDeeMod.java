package net.bdew.wurm.server.threedee;

import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import net.bdew.wurm.server.threedee.actions.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ThreeDeeMod implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener, ItemTemplatesCreatedListener, PlayerMessageListener {
    private static final Logger logger = Logger.getLogger("ThreeDeeMod");

    public static int minPower = 0;

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    public static final Map<Integer, ContainerEntry> containers = new HashMap<>();
    public static Set<Integer> forceContainers;


    @Override
    public void configure(Properties properties) {
        if (properties.containsKey("forceContainers")) {
            forceContainers = Arrays.stream(properties.getProperty("forceContainers").split(","))
                    .map(s -> Integer.parseInt(s.trim(), 10))
                    .collect(Collectors.toSet());
        } else forceContainers = Collections.emptySet();

        for (String key : properties.stringPropertyNames()) {
            try {
                if (key.startsWith("container@")) {
                    String[] split = key.split("@");
                    int id = Integer.parseInt(split[1]);
                    if (properties.getProperty(key).equals("manual-place-only")) {
                        containers.put(id, new ContainerEntry(id, 0, 0, 0, 0, 0, true));
                    } else {
                        split = properties.getProperty(key).split(",");
                        if (split.length != 5) {
                            logWarning(String.format("Unable to parse value %s = %s", key, properties.getProperty(key)));
                        } else {
                            float xSz = Float.parseFloat(split[0]);
                            float ySz = Float.parseFloat(split[1]);
                            float zSz = Float.parseFloat(split[2]);
                            float xOff = Float.parseFloat(split[3]);
                            float yOff = Float.parseFloat(split[4]);
                            containers.put(id, new ContainerEntry(id, xSz, ySz, zSz, xOff, yOff, false));
                        }
                    }
                } else if (key.equals("minPower")) {
                    minPower = Integer.parseInt(properties.getProperty("minPower"));
                }
            } catch (NumberFormatException e) {
                logWarning(String.format("Unable to parse config entry %s = %s", key, properties.getProperty(key)));
            }
        }
    }

    @Override
    public void preInit() {
        try {
            ModActions.init();

            ClassPool classPool = HookManager.getInstance().getClassPool();

            CtClass ctVirtualZone = classPool.getCtClass("com.wurmonline.server.zones.VirtualZone");

            ctVirtualZone.getMethod("addItem", "(Lcom/wurmonline/server/items/Item;Lcom/wurmonline/server/zones/VolaTile;JZ)Z")
                    .instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("sendNewMovingItem") || m.getMethodName().equals("sendItem")) {
                                logInfo(String.format("Hooked %s in VZ.addItem line %d", m.getMethodName(), m.getLineNumber()));
                                m.replace("$_ = $proceed($$); net.bdew.wurm.server.threedee.Hooks.sendItemHook(this.watcher.getCommunicator(), item);");
                            }
                        }
                    });

            ctVirtualZone.getMethod("sendRemoveItem", "(Lcom/wurmonline/server/items/Item;)V")
                    .instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("sendDeleteMovingItem") || m.getMethodName().equals("sendRemoveItem")) {
                                logInfo(String.format("Hooked %s in VZ.sendRemoveItem line %d", m.getMethodName(), m.getLineNumber()));
                                m.replace("$_ = $proceed($$); net.bdew.wurm.server.threedee.Hooks.removeItemHook(this.watcher.getCommunicator(), item);");
                            }
                        }
                    });

            ExprEditor realContainerEditor = new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isHollow")) {
                        logInfo(String.format("Hooked %s in %s.%s line %d", m.getMethodName(), m.where().getDeclaringClass().getSimpleName(), m.where().getName(), m.getLineNumber()));
                        m.replace("$_ = $0.isHollow() && net.bdew.wurm.server.threedee.Hooks.isReallyContainer($0);");
                    }
                }
            };

            CtClass ctItemBehaviour = classPool.getCtClass("com.wurmonline.server.behaviours.ItemBehaviour");
            ctItemBehaviour.getMethod("getBehavioursFor", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;)Ljava/util/List;")
                    .instrument(realContainerEditor);
            ctItemBehaviour.getMethod("getBehavioursFor", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;Lcom/wurmonline/server/items/Item;)Ljava/util/List;")
                    .instrument(realContainerEditor);

            classPool.getCtClass("com.wurmonline.server.spells.Spell")
                    .getMethod("mayBeEnchanted", "(Lcom/wurmonline/server/items/Item;)Z")
                    .instrument(realContainerEditor);

            CtClass ctCommunicator = classPool.getCtClass("com.wurmonline.server.creatures.Communicator");
            CtMethod mSendAddToInventory = ctCommunicator.getMethod("sendAddToInventory", "(Lcom/wurmonline/server/items/Item;JJI)V");
            mSendAddToInventory.insertBefore("if (!net.bdew.wurm.server.threedee.Hooks.inventoryFilter($1)) return;");
            mSendAddToInventory.instrument(realContainerEditor);

            CtConstructor[] actionConstructors = classPool.getCtClass("com.wurmonline.server.behaviours.Action").getConstructors();
            for (CtConstructor constructor : actionConstructors)
                constructor.insertAfter("net.bdew.wurm.server.threedee.Hooks.checkAction(this);");

            CtClass ctItem = classPool.getCtClass("com.wurmonline.server.items.Item");
            ctItem.getMethod("removeItem", "(JZZZ)Lcom/wurmonline/server/items/Item;")
                    .insertAfter("net.bdew.wurm.server.threedee.Hooks.removeFromItemHook(this, $_);");
            ctItem.getMethod("getTopParent", "()J")
                    .insertBefore("if (net.bdew.wurm.server.threedee.Hooks.isParentHook(this)) return this.id;");
            ctItem.getMethod("mayCreatureInsertItem", "()Z").insertBefore("if (!net.bdew.wurm.server.threedee.Hooks.isReallyContainer(this)) return false;");
            ctItem.getMethod("moveToItem", "(Lcom/wurmonline/server/creatures/Creature;JZ)Z")
                    .instrument(new ExprEditor() {
                        boolean found = false;

                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (!found && m.getMethodName().equals("getItem")) {
                                found = true;
                                m.replace("$_=$proceed($$); if (net.bdew.wurm.server.threedee.Hooks.checkAbortMove(this, $_, mover)) return false;");
                            }
                        }
                    });

            ctItem.getMethod("isInsidePlaceableContainer", "()Z").setBody("return false;");

            CtClass ctItemTemplate = classPool.getCtClass("com.wurmonline.server.items.ItemTemplate");
            ctItemTemplate.getMethod("hasViewableSubItems", "()Z").setBody("return false;");

            ctCommunicator.getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;JZ)V")
                    .instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("isInsidePlaceableContainer")) {
                                logInfo(String.format("Hooked %s in %s.%s line %d", m.getMethodName(), m.where().getDeclaringClass().getSimpleName(), m.where().getName(), m.getLineNumber()));
                                m.replace("$_ = net.bdew.wurm.server.threedee.Hooks.isOnSurface($0);");
                            } else if (m.getMethodName().equals("hasViewableSubItems")) {
                                logInfo(String.format("Hooked %s in %s.%s line %d", m.getMethodName(), m.where().getDeclaringClass().getSimpleName(), m.where().getName(), m.getLineNumber()));
                                m.replace("$_ = net.bdew.wurm.server.threedee.Hooks.isSurface($0) && item.getParentId() == -10L;");
                            } else if (m.getMethodName().equals("getParentId")) {
                                logInfo(String.format("Hooked %s in %s.%s line %d", m.getMethodName(), m.where().getDeclaringClass().getSimpleName(), m.where().getName(), m.getLineNumber()));
                                m.replace("$_ = net.bdew.wurm.server.threedee.Hooks.getSurfaceId($0);");
                            }
                        }
                    });

            classPool.getCtClass("com.wurmonline.server.behaviours.MethodsItems")
                    .getMethod("handlePlaceItem", "(Lcom/wurmonline/server/creatures/Creature;JJFFFF)V")
                    .insertBefore("if (parentId!=-10L && itemId!=-10L) {net.bdew.wurm.server.threedee.Hooks.handlePlaceItem($1,$2,$3,$4,$5,$6,$7); return;}");

            classPool.getCtClass("com.wurmonline.server.behaviours.MethodsReligion")
                    .getMethod("canBeSacrificed", "(Lcom/wurmonline/server/items/Item;)Z")
                    .insertBefore("if (net.bdew.wurm.server.threedee.Hooks.isOnSurfaceOrHook($1)) return false;");

        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void onItemTemplatesCreated() {
        try {
            CustomItems.regItems();
            ContainerEntry.initFields();
            for (ContainerEntry ent : containers.values()) {
                ent.overrideTemplateFlags();
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchTemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new PlaceAction());

        ModActions.registerBehaviourProvider(new MoveBehaviourProvider());
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PUSH));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PUSH_GENTLY));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PULL));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PULL_GENTLY));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.MOVE_CENTER));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.TURN_ITEM));
        ModActions.registerActionPerformer(new MoveActionPerformer(Actions.TURN_ITEM_BACK));

        ModActions.registerActionPerformer(new PreventHollowActionPerformer(Actions.OPEN));
        ModActions.registerActionPerformer(new PreventHollowActionPerformer(Actions.CLOSE));

        ModActions.registerActionPerformer(new PreventTakeActionPerformer(Actions.TAKE));
        ModActions.registerActionPerformer(new PreventTakeActionPerformer(Actions.LOAD_CARGO));

        ModActions.registerBehaviourProvider(new OpenCloseBehaviourProvider());

        ModActions.registerActionPerformer(new OpenCloseActionPerformer(Actions.OPEN));
        ModActions.registerActionPerformer(new OpenCloseActionPerformer(Actions.CLOSE));

        ModActions.registerBehaviourProvider(new LockBehaviourProvider());

        try {
            Field fViewableSubItems = ReflectionUtil.getField(Class.forName("com.wurmonline.server.items.ItemTemplate"), "viewableSubItems");
            for (ItemTemplate tpl : ItemTemplateFactory.getInstance().getTemplates()) {
                if (containers.containsKey(tpl.getTemplateId())) continue;
                if (ReflectionUtil.getPrivateField(tpl, fViewableSubItems))
                    logInfo(String.format("Item is surface in vanilla but has no config: %s (%d)", tpl.getName(), tpl.getTemplateId()));
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public boolean onPlayerMessage(Communicator communicator, String message) {
        return false;
    }

    @Override
    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        if (message.startsWith("#surface") && (communicator.getPlayer().getPower() == 5)) {
            return Commands.handleSurface(message, communicator);
        } else return MessagePolicy.PASS;
    }
}
