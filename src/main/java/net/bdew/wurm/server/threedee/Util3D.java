package net.bdew.wurm.server.threedee;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Util3D {
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
                player.getCommunicator().getConnection().flush();
            } catch (Exception ex) {
                ThreeDeeMod.logException(String.format("Failed to send item %s (%d) to player %s (%d)", player.getName(), player.getWurmId(), item.getName(), item.getWurmId()), ex);
                player.setLink(false);
            }
        }
    }

    public static float decodeLinear(float base, int val) {
        return base * 0.0001f * val;
    }

    public static int encodeLinear(float base, float val) {
        return (int) (10000f * val / base);
    }

    public static void forAllHooks(Item item, BiConsumer<Item, Item> func) {
        if (ThreeDeeMod.containers.containsKey(item.getTemplateId())) {
            for (Item hook : item.getItemsAsArray()) {
                if (hook.getTemplateId() == ThreeDeeStuff.hookItem.getTemplateId()) {
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
}
