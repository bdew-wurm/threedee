package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.ContainerEntry;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import net.bdew.wurm.server.threedee.ThreeDeeStuff;

import java.util.Set;

public enum OpenCloseCheck {
    OPEN, CLOSE, NOTHING;

    static OpenCloseCheck getState(Creature performer, Item target) {
        if (!performer.isPlayer() || !target.isHollow() || target.isSealedByPlayer()) return NOTHING;

        ContainerEntry cont = ThreeDeeMod.containers.get(target.getTemplateId());

        if (cont != null && !cont.reallyContainer) return NOTHING;

        Item hook = target.getParentOrNull();
        if (hook == null || hook.getTemplateId() != ThreeDeeStuff.hookItemId) return NOTHING;

        Item parent = hook.getParentOrNull();
        if (parent == null || !ThreeDeeMod.containers.containsKey(parent.getTemplateId()) || parent.getParentId() != -10L)
            return NOTHING;

        // mayUseInventoryOfVehicle actually checks for all items with permissions, not just vehicles
        if ((target.getLockId() != -10L) && (!MethodsItems.mayUseInventoryOfVehicle(performer, target))) return NOTHING;

        Set<Creature> watchers = target.getWatcherSet();

        boolean watched = watchers != null && watchers.stream().anyMatch(c -> c.getWurmId() == performer.getWurmId());

        if (watched)
            return CLOSE;
        else
            return OPEN;
    }
}
