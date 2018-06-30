package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;

public class ContainerEntry {
    public final int templateId;
    public final float sizeX, sizeY, sizeZ, xOffset, yOffset;

    private boolean reallyContainer, reallyLockable, reallyPlantable;

    static Field hollow, lockable, plantable, viewableSubItems;

    public ContainerEntry(int templateId, float sizeX, float sizeY, float sizeZ, float xOffset, float yOffset) {
        this.templateId = templateId;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public static void initFields() throws NoSuchFieldException {
        hollow = ReflectionUtil.getField(ItemTemplate.class, "hollow");
        lockable = ReflectionUtil.getField(ItemTemplate.class, "lockable");
        plantable = ReflectionUtil.getField(ItemTemplate.class, "isPlantable");
        viewableSubItems = ReflectionUtil.getField(ItemTemplate.class, "viewableSubItems");
    }

    public void overrideTemplateFlags() throws NoSuchTemplateException, IllegalAccessException {
        ItemTemplate tpl = getTemplate();

        if (tpl.isHollow())
            reallyContainer = tpl.getTemplateId() == ItemList.woodenBedsideTable || !(boolean) ReflectionUtil.getPrivateField(tpl, viewableSubItems);
        else
            ReflectionUtil.setPrivateField(tpl, hollow, true);

        if (tpl.isLockable())
            reallyLockable = true;
        else
            ReflectionUtil.setPrivateField(tpl, lockable, true);

        if (tpl.isPlantable())
            reallyPlantable = true;
        else
            ReflectionUtil.setPrivateField(tpl, plantable, true);
    }

    public void restoreTemplateFlags() throws NoSuchTemplateException, IllegalAccessException {
        ItemTemplate tpl = getTemplate();
        if (!reallyContainer) ReflectionUtil.setPrivateField(tpl, hollow, false);
        if (!reallyLockable) ReflectionUtil.setPrivateField(tpl, lockable, false);
        if (!reallyPlantable) ReflectionUtil.setPrivateField(tpl, plantable, false);
    }

    public ContainerEntry resized(float newSizeX, float newSizeY, float newSizeZ, float newXOffset, float newYOffset) {
        ContainerEntry tmp = new ContainerEntry(templateId, newSizeX, newSizeY, newSizeZ, newXOffset, newYOffset);
        tmp.reallyPlantable = reallyPlantable;
        tmp.reallyLockable = reallyLockable;
        tmp.reallyContainer = reallyContainer;
        return tmp;
    }

    public boolean isReallyContainer() {
        return reallyContainer;
    }

    public ItemTemplate getTemplate() throws NoSuchTemplateException {
        return ItemTemplateFactory.getInstance().getTemplate(templateId);
    }
}
