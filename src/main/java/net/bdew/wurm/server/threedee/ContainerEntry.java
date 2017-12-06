package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

public class ContainerEntry {
    public final int templateId;
    public final float sizeX, sizeY, sizeZ;

    public ContainerEntry(int templateId, float sizeX, float sizeY, float sizeZ) {
        this.templateId = templateId;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public ItemTemplate getTemplate() throws NoSuchTemplateException {
        return ItemTemplateFactory.getInstance().getTemplate(templateId);
    }
}
