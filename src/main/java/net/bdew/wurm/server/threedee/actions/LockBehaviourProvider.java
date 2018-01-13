package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.ContainerEntry;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;

import java.util.Collections;
import java.util.List;

public class LockBehaviourProvider implements BehaviourProvider {


    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        if (target.getParentId() != -10L || !target.isOwner(performer)) return null;

        ContainerEntry ent = ThreeDeeMod.containers.get(target.getTemplateId());
        if (ent == null || ent.isReallyContainer()) return null;

        long lockId = target.getLockId();
        Item oldLock = null;
        if (lockId != -10L) {
            try {
                oldLock = Items.getItem(lockId);
            } catch (NoSuchItemException ignored) {
            }
        }

        if (source != null && source.isLock()) {
            if (oldLock == null) {
                return Collections.singletonList(Actions.actionEntrys[Actions.SET_LOCK]);
            } else {
                return Collections.singletonList((new ActionEntry(Actions.REPLACE, "Replace lock", "replacing lock")));
            }
        } else if (oldLock != null) {
            return Collections.singletonList(Actions.actionEntrys[Actions.UNLOCK]);
        }

        return null;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        return getBehavioursFor(performer, null, target);

    }
}
