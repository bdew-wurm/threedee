package net.bdew.wurm.server.threedee;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;

public class Hooks {
    public static void sendItemHook(Communicator comm, Item item) {
        if (ThreeDeeMod.containers.containsKey(item.getTemplateId())) {
            for (Item hook : item.getItemsAsArray()) {
                if (hook.getTemplateId() == ThreeDeeStuff.hookItem.getTemplateId()) {
                    Item[] sub = hook.getItemsAsArray();
                    if (sub.length > 0) {
                        try {
                            PosData pos = PosData.from(hook);
                            Util3D.sendItem(comm.player, sub[0], item.getPosX() + pos.x, item.getPosY() + pos.y, item.getPosZ() + pos.z, MovementScheme.normalizeAngle(item.getRotation() + pos.rot));
                        } catch (InvalidHookError e) {
                            ThreeDeeMod.logException("Error sending hook", e);
                        }
                    }
                }
            }
        }
    }
}
