package net.bdew.wurm.server.threedee;

import com.wurmonline.server.items.Item;

public class PosData {
    public float x, y, z, rot;

    public PosData(float x, float y, float z, float rot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rot = rot;
    }

    public static PosData fromOldHook(Item hook, Item parent) {
        int parentTpl = parent.getTemplateId();
        ContainerEntry cont = ThreeDeeMod.containers.get(parentTpl);
        if (cont == null)
            return new PosData(0, 0, 0, 0);
        return new PosData(Utils.decodeLinear(cont.sizeX, hook.getData1()) + cont.xOffset, Utils.decodeLinear(cont.sizeY, hook.getData2()) + cont.yOffset, cont.sizeZ, hook.getAuxData() * 22.5f);
    }

    public static PosData from(Item item) {
        return new PosData(item.getPosXRaw(), item.getPosYRaw(), item.getPosZRaw(), item.getRotation());
    }

    public void saveToItem(Item item) {
        item.setPos(x, y, z, rot, item.getBridgeId());
    }
}
