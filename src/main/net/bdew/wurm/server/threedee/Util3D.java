package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.nio.ByteBuffer;

public class Util3D {
    public static void sendItem(Player player, Item item, float x, float y, float z) {
        if (player.hasLink() && item.getTemplateId() != 520) {
            try {
                final long id = item.getWurmId();
                final ByteBuffer bb = player.getCommunicator().getConnection().getBuffer();

                bb.put((byte) (-9));

                bb.putLong(id);
                bb.putFloat(x);
                bb.putFloat(y);
                bb.putFloat(item.getRotation());
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
}
