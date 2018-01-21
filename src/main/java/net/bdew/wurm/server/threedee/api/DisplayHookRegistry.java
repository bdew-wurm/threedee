package net.bdew.wurm.server.threedee.api;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;

import java.util.HashMap;
import java.util.Map;

public class DisplayHookRegistry {
    public static Map<Integer, IDisplayHook> hooks = new HashMap<>();

    public static void add(int templateId, IDisplayHook hook) {
        hooks.put(templateId, hook);
    }

    public static boolean doAddItem(Communicator comm, Item item, float x, float y, float z, float rot) {
        IDisplayHook hook = hooks.get(item.getTemplateId());
        return hook != null && hook.addItem(comm, item, x, y, z, rot);
    }

    public static boolean doRemoveItem(Communicator comm, Item item) {
        IDisplayHook hook = hooks.get(item.getTemplateId());
        return hook != null && hook.removeItem(comm, item);
    }
}
