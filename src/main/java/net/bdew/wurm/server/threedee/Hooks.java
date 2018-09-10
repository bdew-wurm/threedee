package net.bdew.wurm.server.threedee;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.shared.constants.CounterTypes;
import net.bdew.wurm.server.threedee.api.DisplayHookRegistry;

import java.util.Set;

public class Hooks {
    public static void sendItemHook(Communicator comm, Item item) {
        double cs = Math.cos(item.getRotation() * Math.PI / 180f);
        double sn = Math.sin(item.getRotation() * Math.PI / 180f);

        Utils.forAllHooks(item, (hook, sub) -> {
            if (!hook.getAuxBit(7))
                Utils.convertHook(hook, sub);

            float x = (float) (item.getPosX() + cs * sub.getPosXRaw() - sn * sub.getPosYRaw());
            float y = (float) (item.getPosY() + sn * sub.getPosXRaw() + cs * sub.getPosYRaw());
            float z = item.getPosZ() + sub.getPosZRaw();
            float rot = MovementScheme.normalizeAngle(item.getRotation() + sub.getRotation());

            if (!DisplayHookRegistry.doAddItem(comm, sub, x, y, z, rot)) {
                if (item.isMovingItem()) {
                    Utils.sendItem(comm.getPlayer(), sub, x, y, z, rot);
                } else {
                    comm.sendItem(sub, -10L, false);
                }
                Utils.sendExtras(comm.getPlayer(), sub);
            }
        });
    }


    private static void doRemoveItem(Communicator comm, Item item) {
        if (!DisplayHookRegistry.doRemoveItem(comm, item))
            comm.sendRemoveItem(item);
    }

    public static void removeItemHook(Communicator comm, Item item) {
        Utils.forAllHooks(item, (hook, sub) -> doRemoveItem(comm, sub));
    }

    public static void removeFromItemHook(Item item, Item ret) {
        if (item.getTemplateId() == CustomItems.hookItemId) {
            Item parent = item.getParentOrNull();
            if (parent != null && parent.getParentId() == -10) {
                Utils.forAllWatchers(parent, (player) -> doRemoveItem(player.getCommunicator(), ret));
            }
            if (ret.getParentId() == item.getWurmId())
                ret.setParentId(item.getParentId(), item.isOnSurface());
            Set<Creature> watchers = ret.getWatcherSet();
            if (watchers != null) {
                for (Creature creature : watchers) {
                    if (creature.getCommunicator().sendCloseInventoryWindow(ret.getWurmId())) {
                        ret.removeWatcher(creature, true);
                    }
                }
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

    public static boolean checkAbortMove(Item source, Item target, Creature performer) {
        if (isReallyContainer(target)) return false;
        if (target.getParentId() == -10L) {
            if (Utils.canPlaceOnSurface(performer, source, target)) {
                try {
                    Utils.doPlaceOnSurface(source, target, performer);
                } catch (FailedException | NoSuchTemplateException | NoSuchItemException e) {
                    ThreeDeeMod.logException("Error placing item", e);
                    performer.getCommunicator().sendAlertServerMessage("Placing failed, try again later or contact staff.");
                }
            } else {
                performer.getCommunicator().sendAlertServerMessage(String.format("You are not allowed to place the %s on the %s.", source.getName(), target.getName()));
            }
        }
        return true;
    }

    public static boolean isSurface(ItemTemplate tpl) {
        return ThreeDeeMod.containers.containsKey(tpl.getTemplateId());
    }

    public static boolean isOnSurface(Item item) {
        Item parent = item.getParentOrNull();
        if (parent == null || parent.getTemplateId() != CustomItems.hookItemId) return false;
        Item top = parent.getParentOrNull();
        if (top == null || !isSurface(top.getTemplate())) return false;
        return parent.getTopParentOrNull() == top;
    }

    public static boolean isOnSurfaceOrHook(Item item) {
        if (item.getTemplateId() == CustomItems.hookItemId) return true;
        Item parent = item.getParentOrNull();
        return parent != null && parent.getTemplateId() == CustomItems.hookItemId;
    }


    public static long getSurfaceId(Item item) {
        Item parent = item.getParentOrNull();
        if (parent == null || parent.getTemplateId() != CustomItems.hookItemId) return -10L;
        return parent.getParentId();
    }

    public static void handlePlaceItem(Creature performer, long itemId, long parentId, float xPos, float yPos, float zPos, float rot) {
        if (performer.getPlacementItem() != null && performer.getPlacementItem().getWurmId() == itemId) {
            performer.getCommunicator().sendNormalServerMessage("You must place the item from your inventory to put it there.");
            performer.setPlacingItem(false);
            return;
        }
        if (!performer.isPlacingItem()) {
            performer.getCommunicator().sendNormalServerMessage("An error occured while placing that item.");
            return;
        }

        performer.setPlacingItem(false);

        try {
            Item item = Items.getItem(itemId);
            Item parent = Items.getItem(parentId);

            if (!Utils.canPlaceOnSurface(performer, item, parent)) {
                performer.getCommunicator().sendNormalServerMessage("You are not allowed to place the item.");
                return;
            }

            Utils.doPlaceOnSurfacePos(item, parent, performer, xPos, yPos, zPos, rot);

        } catch (Exception e) {
            performer.getCommunicator().sendNormalServerMessage("Error while placing item, try again later or contact staff.");
        }
    }
}
