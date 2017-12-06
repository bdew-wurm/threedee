package net.bdew.wurm.server.threedee;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.Item;

public class Hooks {
    public static void sendItemHook(Communicator comm, Item item) {
        if (item.getTemplateId() == ThreeDeeStuff.containerTable.getTemplateId()) {
            Item[] content = item.getItemsAsArray();
            if (content.length > 0) {
                float x = item.getPosX();
                float y = item.getPosY();
                float z = item.getPosZ() + 1;
                Util3D.sendItem(comm.player, content[0], x, y, z);
            }
        }
    }
}
