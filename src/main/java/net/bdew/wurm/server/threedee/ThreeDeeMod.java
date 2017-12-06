package net.bdew.wurm.server.threedee;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreeDeeMod implements WurmServerMod, Initable, PreInitable, ServerStartedListener {
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

    @Override
    public void preInit() {
        try {
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
    public void onServerStarted() {
        try {
            ThreeDeeStuff.regItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
