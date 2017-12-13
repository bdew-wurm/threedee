package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;

public class ThreeDeeStuff {
    public static int hookItemId;
    public static ItemTemplate hookItem;

    public static void installHook(ClassPool cp) throws NotFoundException, CannotCompileException {
        CtClass ctCommunicator = cp.getCtClass("com.wurmonline.server.creatures.Communicator");
        ctCommunicator.getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;JZ)V")
                .insertAfter("net.bdew.wurm.server.threedee.Hooks.sendItemHook(this, $1);");
        ctCommunicator.getMethod("sendRemoveItem", "(Lcom/wurmonline/server/items/Item;)V")
                .insertAfter("net.bdew.wurm.server.threedee.Hooks.removeItemHook(this, $1);");
        ctCommunicator.getMethod("sendAddToInventory", "(Lcom/wurmonline/server/items/Item;JJI)V")
                .insertBefore("if (!net.bdew.wurm.server.threedee.Hooks.inventoryFilter($1)) return;");
        ExprEditor realContainerEditor = new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals("isHollow"))
                    m.replace("$_ = $0.isHollow() && net.bdew.wurm.server.threedee.Hooks.isReallyContainer($0);");
            }
        };

        CtClass ctItemBehaviour = cp.getCtClass("com.wurmonline.server.behaviours.ItemBehaviour");
        ctItemBehaviour.getMethod("getBehavioursFor", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;)Ljava/util/List;")
                .instrument(realContainerEditor);
        ctItemBehaviour.getMethod("getBehavioursFor", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;Lcom/wurmonline/server/items/Item;)Ljava/util/List;")
                .instrument(realContainerEditor);

        cp.getCtClass("com.wurmonline.server.items.Item")
                .getMethod("removeItem", "(JZZZ)Lcom/wurmonline/server/items/Item;")
                .insertAfter("net.bdew.wurm.server.threedee.Hooks.removeFromItemHook(this, $_);");
    }

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