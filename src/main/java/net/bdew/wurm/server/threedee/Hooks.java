package net.bdew.wurm.server.threedee;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;

public class Hooks {
    public static void sendItemHook(Communicator comm, Item item) {
        double cs = Math.cos(item.getRotation() * Math.PI / 180f);
        double sn = Math.sin(item.getRotation() * Math.PI / 180f);

        Util3D.forAllHooks(item, (hook, sub) -> {
            try {
                PosData pos = PosData.from(hook);
                float x = (float) (item.getPosX() + cs * pos.x - sn * pos.y);
                float y = (float) (item.getPosY() + sn * pos.x + cs * pos.y);
                Util3D.sendItem(comm.player, sub, x, y, item.getPosZ() + pos.z, MovementScheme.normalizeAngle(item.getRotation() + pos.rot));
            } catch (InvalidHookError e) {
                ThreeDeeMod.logException("Error sending hook", e);
            }
        });
    }


    public static void removeItemHook(Communicator comm, Item item) {
        Util3D.forAllHooks(item, (hook, sub) -> comm.sendRemoveItem(sub));
    }

    public static void removeFromItemHook(Item item, Item ret) {
        if (item.getTemplateId() == ThreeDeeStuff.hookItemId) {
            Item parent = item.getParentOrNull();
            if (parent != null && parent.getParentId() == -10) {
                Util3D.forAllWatchers(parent, (player) -> player.getCommunicator().sendRemoveItem(ret));
            }
            if (item.getItemsAsArray().length == 0)
                Items.destroyItem(item.getWurmId());
        }
    }

    public static boolean inventoryFilter(Item item) {
        return item.getTemplateId() != ThreeDeeStuff.hookItemId;
    }


    public static boolean isReallyContainer(Item item) {
        ContainerEntry ent = ThreeDeeMod.containers.get(item.getTemplateId());
        return ent == null || ent.reallyContainer;
    }

}
