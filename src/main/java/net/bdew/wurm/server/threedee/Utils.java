package net.bdew.wurm.server.threedee;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Utils {
    public static void sendItem(Player player, Item item, float x, float y, float z, float rot) {
        if (player.hasLink() && item.getTemplateId() != 520) {
            try {
                final long id = item.getWurmId();
                final ByteBuffer bb = player.getCommunicator().getConnection().getBuffer();

                bb.put((byte) (-9));

                bb.putLong(id);
                bb.putFloat(x);
                bb.putFloat(y);
                bb.putFloat(rot);
                bb.putFloat(z);

                byte[] tempStringArr = item.getName().getBytes("UTF-8");
                bb.put((byte) tempStringArr.length);
                bb.put(tempStringArr);

                tempStringArr = item.getHoverText().getBytes("UTF-8");
                bb.put((byte) tempStringArr.length);
                bb.put(tempStringArr);

                tempStringArr = item.getModelName().getBytes("UTF-8");
                bb.put((byte) tempStringArr.length);
                bb.put(tempStringArr);

                bb.put((byte) (item.isOnSurface() ? 0 : -1));

                bb.put(item.getMaterial());

                tempStringArr = item.getDescription().getBytes("UTF-8");
                bb.put((byte) tempStringArr.length);
                bb.put(tempStringArr);

                bb.putShort(item.getImageNumber());

                if (item.getTemplateId() == 177) {
                    bb.put((byte) 0);
                } else {
                    bb.put((byte) 1);
                    bb.putFloat(item.getQualityLevel());
                    bb.putFloat(item.getDamage());
                }

                bb.putFloat(item.getSizeMod());
                bb.putLong(item.onBridge());
                bb.put(item.getRarity());

                bb.put((byte) 0);
                if (item.hasExtraData()) {
                    bb.put((byte) 1);
                    bb.putInt(item.getExtra1());
                    bb.putInt(item.getExtra2());
                } else {
                    bb.put((byte) 0);
                }

                player.getCommunicator().getConnection().flush();

                sendExtras(player, item);

            } catch (Exception ex) {
                ThreeDeeMod.logException(String.format("Failed to send item %s (%d) to player %s (%d)", player.getName(), player.getWurmId(), item.getName(), item.getWurmId()), ex);
                player.setLink(false);
            }
        }
    }

    public static void sendExtras(Player player, Item item) {
        if (item.isLight()) {
            if (item.isOnFire()) {
                int lightStrength;
                if (item.color != -1) {
                    lightStrength = Math.max(WurmColor.getColorRed(item.color), WurmColor.getColorGreen(item.color));
                    lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(item.color)));
                    byte r = (byte) (WurmColor.getColorRed(item.color) * 128 / lightStrength);
                    byte g = (byte) (WurmColor.getColorGreen(item.color) * 128 / lightStrength);
                    byte b = (byte) (WurmColor.getColorBlue(item.color) * 128 / lightStrength);
                    player.getCommunicator().sendAttachEffect(item.getWurmId(), (byte) 4, r, g, b, item.getRadius());
                } else if (item.isLightBright()) {
                    lightStrength = (int) (80.0F + item.getCurrentQualityLevel() / 100.0F * 40.0F);
                    player.getCommunicator().sendAttachEffect(item.getWurmId(), (byte) 4, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), item.getRadius());
                } else {
                    player.getCommunicator().sendAttachEffect(item.getWurmId(), (byte) 4, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), item.getRadius());
                }
            }
        } else if (item.color != -1) {
            player.getCommunicator().sendRepaint(item.getWurmId(), (byte) WurmColor.getColorRed(item.color), (byte) WurmColor.getColorGreen(item.color), (byte) WurmColor.getColorBlue(item.color), (byte) -1, (byte) 0);
        }
    }

    public static float decodeLinear(float base, int val) {
        return base * 0.0001f * val;
    }

    public static void forAllHooks(Item item, BiConsumer<Item, Item> func) {
        if (ThreeDeeMod.containers.containsKey(item.getTemplateId())) {
            for (Item hook : item.getItemsAsArray()) {
                if (hook.getTemplateId() == CustomItems.hookItemId) {
                    for (Item sub : hook.getItemsAsArray())
                        func.accept(hook, sub);
                }
            }
        }
    }

    public static void forAllWatchers(Item watched, Consumer<Player> func) {
        if (watched.getParentId() != -10) return;
        VolaTile tile = Zones.getOrCreateTile(watched.getTilePos(), watched.isOnSurface());
        for (VirtualZone vz : tile.getWatchers()) {
            Creature watcher = vz.getWatcher();
            if (watcher.isPlayer() && watcher.hasLink())
                func.accept((Player) watcher);
        }
    }

    public static boolean canAccessContainer(Item container, Creature performer) {
        return container != null && performer != null && container.getParentId() == -10L && (container.isOwner(performer) || !container.isLocked() || container.mayAccessHold(performer));
    }

    public static boolean canAccessPlacedItem(Item item, Creature performer) {
        if (item == null || performer == null) return false;
        Item parent = item.getParentOrNull();
        if (parent == null || parent.getTemplateId() != CustomItems.hookItemId) return false;
        if (item.isOwner(performer)) return true;
        Item top = parent.getParentOrNull();
        return top != null && ThreeDeeMod.containers.containsKey(top.getTemplateId()) && canAccessContainer(top, performer);
    }

    public static boolean canPlaceOnSurface(Creature performer, Item source, Item target) {
        return (performer.isPlayer()) && (performer.getPower() >= ThreeDeeMod.minPower) &&
                (source != null) && (source.getTopParentOrNull() == performer.getInventory()) && (source.canBeDropped(true) && !source.isLiquid()) &&
                (target != null) && (ThreeDeeMod.containers.containsKey(target.getTemplateId()) && (target.getParentId() == -10)) &&
                canAccessContainer(target, performer);
    }

    public static void doPlaceOnSurface(Item source, Item target, Creature performer) throws NoSuchTemplateException, FailedException, NoSuchItemException {
        ContainerEntry cont = ThreeDeeMod.containers.get(target.getTemplateId());
        doPlaceOnSurfacePos(source, target, performer, cont.xOffset, cont.yOffset, cont.sizeZ, 0);
    }

    public static void doPlaceOnSurfacePos(Item source, Item target, Creature performer, float xPos, float yPos, float zPos, float rot) throws NoSuchTemplateException, FailedException, NoSuchItemException {
        if (performer.getPower() > 0)
            performer.getCommunicator().sendNormalServerMessage(String.format("Placed at %.3f,%.3f,%.3f", xPos, yPos, zPos));
        Item hook = ItemFactory.createItem(CustomItems.hookItemId, 99f, null);
        source.getParent().dropItem(source.getWurmId(), false);
        hook.insertItem(source, true, false);
        hook.setAuxBit(7, true);
        target.insertItem(hook, true, false);
        source.setLastOwnerId(performer.getWurmId());
        source.setPos(xPos, yPos, zPos, rot, target.getBridgeId());
        forAllWatchers(target, player -> Hooks.sendItemHook(player.getCommunicator(), target));
    }

    public static void convertHook(Item hook, Item sub) {
        Item parent = hook.getParentOrNull();
        if (parent != null) {
            sub.setOnBridge(parent.getBridgeId());
            PosData.fromOldHook(hook, parent).saveToItem(sub);
        }
        hook.setData(-10L);
        hook.setAuxData((byte) 0x80);
    }
}
