package net.bdew.wurm.server.threedee;

import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import net.bdew.wurm.server.threedee.actions.MoveActionPerformer;
import net.bdew.wurm.server.threedee.actions.MoveBehaviourProvider;
import net.bdew.wurm.server.threedee.actions.PlaceAction;
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

public class ThreeDeeMod implements WurmServerMod, Initable, PreInitable, ServerStartedListener, ItemTemplatesCreatedListener, Configurable {
    private static final Logger logger = Logger.getLogger("ThreeDeeMod");

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
            if (key.startsWith("container@")) {
                String[] split = key.split("@");
                int id = Integer.parseInt(split[1]);
                split = properties.getProperty(key).split(",");
                if (split.length != 3) {
                    logWarning(String.format("Unable to parse value %s = %s", key, properties.getProperty(key)));
                } else {
                    try {
                        containers.put(id, new ContainerEntry(id, Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2])));
                    } catch (NumberFormatException e) {
                        logWarning(String.format("Unable to parse value %s = %s", key, properties.getProperty(key)));
                    }
                }
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
            Field hollow = ReflectionUtil.getField(ItemTemplate.class, "hollow");
            for (ContainerEntry ent: containers.values()) {
                ReflectionUtil.setPrivateField(ent.getTemplate(), hollow, true);
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchTemplateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServerStarted() {
        try {
            ModActions.registerAction(new PlaceAction());
            ModActions.registerBehaviourProvider(new MoveBehaviourProvider());
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PUSH));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PUSH_GENTLY));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PULL));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.PULL_GENTLY));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.MOVE_CENTER));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.TURN_ITEM));
            ModActions.registerActionPerformer(new MoveActionPerformer(Actions.TURN_ITEM_BACK));
            ThreeDeeStuff.regItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
