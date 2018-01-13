package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;

public class CustomItems {
    public static int hookItemId;
    public static ItemTemplate hookItem;

    public static void regItems() throws IOException {
        hookItem = new ItemTemplateBuilder("bdew.3d.hook")
                .name("item hook", "item hooks", "This is an invisible item used to manage stuff placed on stuff. Because reasons.")
                .imageNumber((short) 0)
                .behaviourType((short) 1)
                .decayTime(Long.MAX_VALUE)
                .weightGrams(1)
                .dimensions(1, 1, 1)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_INDESTRUCTIBLE,
                        ItemTypes.ITEM_TYPE_NOMOVE,
                        ItemTypes.ITEM_TYPE_NOTAKE,
                        ItemTypes.ITEM_TYPE_HOLLOW,
                        ItemTypes.ITEM_TYPE_HASDATA
                })
                .build();

        hookItemId = hookItem.getTemplateId();
    }
}