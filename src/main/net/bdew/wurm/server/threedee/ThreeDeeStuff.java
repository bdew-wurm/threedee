package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;

public class ThreeDeeStuff {
    public static ItemTemplate containerTable;

    public static void installHook(ClassPool cp) throws NotFoundException, CannotCompileException {
        cp.getCtClass("com.wurmonline.server.creatures.Communicator")
                .getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;JZ)V")
                .insertAfter("net.bdew.wurm.server.threedee.Hooks.sendItemHook(this, $1);");
    }

    public static void regItems() throws IOException {
        containerTable = new ItemTemplateBuilder("bdew.3d.table")
                .name("container table", "container tables", "An magic table that can hold stuff on top. The horror!")
                .imageNumber((short) 60)
                .behaviourType((short) 1)
                .decayTime(9072000L)
                .weightGrams(10000)
                .dimensions(10, 60, 60)
                .difficulty(15)
                .value(10000)
                .itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_WOOD,
                        ItemTypes.ITEM_TYPE_HOLLOW,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        ItemTypes.ITEM_TYPE_NAMED,
                        ItemTypes.ITEM_TYPE_DESTROYABLE,
                        ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
                        ItemTypes.ITEM_TYPE_NOTAKE,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_NOT_MISSION,
                        ItemTypes.ITEM_TYPE_COLORABLE,
                        ItemTypes.ITEM_TYPE_TRANSPORTABLE,
                        ItemTypes.ITEM_TYPE_PLANTABLE

                })
                .material(ItemMaterials.MATERIAL_WOOD_BIRCH)
                .modelName("model.furniture.table.square.small.")
                .build();

    }
}