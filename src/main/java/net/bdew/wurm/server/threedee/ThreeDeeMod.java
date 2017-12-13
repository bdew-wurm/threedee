package net.bdew.wurm.server.threedee;

import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import net.bdew.wurm.server.threedee.actions.MoveActionPerformer;
import net.bdew.wurm.server.threedee.actions.MoveBehaviourProvider;
import net.bdew.wurm.server.threedee.actions.PlaceAction;
import net.bdew.wurm.server.threedee.actions.PreventHollowActionPerformer;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreeDeeMod implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener, ItemTemplatesCreatedListener, PlayerMessageListener {
    private static final Logger logger = Logger.getLogger("ThreeDeeMod");

    static Field hollow;

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

    @Override
    public void configure(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            try {
                if (key.startsWith("container@")) {
                    String[] split = key.split("@");
                    int id = Integer.parseInt(split[1]);
                    split = properties.getProperty(key).split(",");
                    if (split.length != 5) {
                        logWarning(String.format("Unable to parse value %s = %s", key, properties.getProperty(key)));
                    } else {
                        float xSz = Float.parseFloat(split[0]);
                        float ySz = Float.parseFloat(split[1]);
                        float zSz = Float.parseFloat(split[2]);
                        float xOff = Float.parseFloat(split[3]);
                        float yOff = Float.parseFloat(split[4]);
                        containers.put(id, new ContainerEntry(id, xSz, ySz, zSz, xOff, yOff));
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
            ThreeDeeStuff.installHook(classPool);
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
            ThreeDeeStuff.regItems();
            hollow = ReflectionUtil.getField(ItemTemplate.class, "hollow");
            for (ContainerEntry ent : containers.values()) {
                if (ent.getTemplate().isHollow())
                    ent.reallyContainer = true;
                else
                    ReflectionUtil.setPrivateField(ent.getTemplate(), hollow, true);
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
    }

    @Override
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
