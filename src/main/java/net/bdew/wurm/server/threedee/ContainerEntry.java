package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

public class ContainerEntry {
    public final int templateId;
    public final float sizeX, sizeY, sizeZ, xOffset, yOffset;
    public boolean reallyContainer;

    public ContainerEntry(int templateId, float sizeX, float sizeY, float sizeZ, float xOffset, float yOffset) {
        this.templateId = templateId;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public ItemTemplate getTemplate() throws NoSuchTemplateException {
        return ItemTemplateFactory.getInstance().getTemplate(templateId);
    }
}
