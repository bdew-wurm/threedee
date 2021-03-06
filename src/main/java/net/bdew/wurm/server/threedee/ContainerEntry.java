package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;

public class ContainerEntry {
    public final int templateId;
    public final String idText;
    public final float sizeX;
    public final float sizeY;
    public final float sizeZ;
    public final float xOffset;
    public final float yOffset;
    public final boolean manualOnly;

    private boolean reallyContainer, reallyLockable, reallyPlantable;

    static Field hollow, lockable, plantable, viewableSubItems, isContainerWithSubItems;

    public ContainerEntry(int templateId, String idText, float sizeX, float sizeY, float sizeZ, float xOffset, float yOffset, boolean manualOnly) {
        this.templateId = templateId;
        this.idText = idText;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.manualOnly = manualOnly;
    }

    public static void initFields() throws NoSuchFieldException {
        hollow = ReflectionUtil.getField(ItemTemplate.class, "hollow");
        lockable = ReflectionUtil.getField(ItemTemplate.class, "lockable");
        plantable = ReflectionUtil.getField(ItemTemplate.class, "isPlantable");
        viewableSubItems = ReflectionUtil.getField(ItemTemplate.class, "viewableSubItems");
        isContainerWithSubItems = ReflectionUtil.getField(ItemTemplate.class, "isContainerWithSubItems");
    }

    public void overrideTemplateFlags() throws NoSuchTemplateException, IllegalAccessException {
        ItemTemplate tpl = getTemplate();

        if (tpl.isHollow())
            reallyContainer = ThreeDeeMod.forceContainers.contains(templateId) || (!(boolean) ReflectionUtil.getPrivateField(tpl, viewableSubItems)) || ((boolean) ReflectionUtil.getPrivateField(tpl, isContainerWithSubItems));
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
        ContainerEntry tmp = new ContainerEntry(templateId, idText, newSizeX, newSizeY, newSizeZ, newXOffset, newYOffset, false);
        tmp.reallyPlantable = reallyPlantable;
        tmp.reallyLockable = reallyLockable;
        tmp.reallyContainer = reallyContainer;
        return tmp;
    }

    public ContainerEntry asManualOnly() {
        ContainerEntry tmp = new ContainerEntry(templateId, idText, 0, 0, 0, 0, 0, true);
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
