package net.bdew.wurm.server.threedee;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.constants.CounterTypes;
import net.bdew.wurm.server.threedee.api.DisplayHookRegistry;

public class Hooks {
    public static void sendItemHook(Communicator comm, Item item) {
        double cs = Math.cos(item.getRotation() * Math.PI / 180f);
        double sn = Math.sin(item.getRotation() * Math.PI / 180f);

        Utils.forAllHooks(item, (hook, sub) -> {
            try {
                PosData pos = PosData.from(hook);
                float x = (float) (item.getPosX() + cs * pos.x - sn * pos.y);
                float y = (float) (item.getPosY() + sn * pos.x + cs * pos.y);
                float z = item.getPosZ() + pos.z;
                float rot = MovementScheme.normalizeAngle(item.getRotation() + pos.rot);
                if (!DisplayHookRegistry.doAddItem(comm, sub, x, y, z, rot))
                    Utils.sendItem(comm.player, sub, x, y, z, rot);
            } catch (InvalidHookError e) {
                ThreeDeeMod.logException("Error sending hook", e);
            }
        });
    }


    public static void removeItemHook(Communicator comm, Item item) {
        Utils.forAllHooks(item, (hook, sub) -> {
            if (!DisplayHookRegistry.doRemoveItem(comm, sub))
                        comm.sendRemoveItem(sub);
                }
        );
    }

    public static void removeFromItemHook(Item item, Item ret) {
        if (item.getTemplateId() == CustomItems.hookItemId) {
            Item parent = item.getParentOrNull();
            if (parent != null && parent.getParentId() == -10) {
                Utils.forAllWatchers(parent, (player) -> player.getCommunicator().sendRemoveItem(ret));
            }
            if (item.getItemsAsArray().length == 0)
                Items.destroyItem(item.getWurmId());
        }
    }

    public static boolean inventoryFilter(Item item) {
        return item.getTemplateId() != CustomItems.hookItemId;
    }


    public static boolean isReallyContainer(Item item) {
        ContainerEntry ent = ThreeDeeMod.containers.get(item.getTemplateId());
        return ent == null || ent.isReallyContainer();
    }

    public static void checkAction(Action act) throws FailedException {
        if (act.getTargetType() != CounterTypes.COUNTER_TYPE_ITEMS || act.getNumber() >= 2000 || act.getNumber() == Actions.EXAMINE || act.getNumber() == Actions.TASTE)
            return;
        try {
            Item item = Items.getItem(act.getTarget());
            Item parent = item.getParentOrNull();
            if (parent != null && parent.getTemplateId() == CustomItems.hookItemId)
                if (!Utils.canAccessPlacedItem(item, act.getPerformer()))
                    throw new FailedException(String.format("You are not allowed to %s the %s.", act.getActionEntry().getActionString().toLowerCase(), item.getName()));
        } catch (NoSuchItemException ignored) {
        }
    }

    public static boolean isParentHook(Item item) {
        Item parent = item.getParentOrNull();
        return parent != null && parent.getTemplateId() == CustomItems.hookItemId;
    }
}
