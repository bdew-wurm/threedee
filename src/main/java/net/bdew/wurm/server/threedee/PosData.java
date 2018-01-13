package net.bdew.wurm.server.threedee;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;

public class PosData {
    public float x, y, z, rot;

    public PosData(float x, float y, float z, float rot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rot = rot;
    }

    public static PosData from(Item hook) throws InvalidHookError {
        try {
            int parentTpl = hook.getParent().getTemplateId();
            ContainerEntry cont = ThreeDeeMod.containers.get(parentTpl);
            if (cont == null)
                throw new InvalidHookError(String.format("Item template %d is not a container", parentTpl));
            return new PosData(Utils.decodeLinear(cont.sizeX, hook.getData1()) + cont.xOffset, Utils.decodeLinear(cont.sizeY, hook.getData2()) + cont.yOffset, cont.sizeZ, hook.getAuxData() * 22.5f);
        } catch (NoSuchItemException e) {
            throw new InvalidHookError("Hook has no parent", e);
        }
    }

    public void saveToItem(Item hook) throws InvalidHookError {
        try {
            int parentTpl = hook.getParent().getTemplateId();
            ContainerEntry cont = ThreeDeeMod.containers.get(parentTpl);
            if (cont == null)
                throw new InvalidHookError(String.format("Item template %d is not a container", parentTpl));
            hook.setData(Utils.encodeLinear(cont.sizeX, x - cont.xOffset), Utils.encodeLinear(cont.sizeY, y - cont.yOffset));
            hook.setAuxData((byte) (rot / 22.5));
        } catch (NoSuchItemException e) {
            throw new InvalidHookError("Hook has no parent", e);
        }
    }
}
