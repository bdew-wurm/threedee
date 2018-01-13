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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenCloseBehaviourProvider implements BehaviourProvider {
    private final List<ActionEntry> openList, closeList;

    public OpenCloseBehaviourProvider() {
        openList = Collections.singletonList(Actions.actionEntrys[Actions.OPEN]);
        closeList = Collections.singletonList(Actions.actionEntrys[Actions.CLOSE]);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> res = getBehavioursFor(performer, target);

        if (source.isLock()) {
            ContainerEntry ent = ThreeDeeMod.containers.get(target.getTemplateId());
            if (ent != null && !ent.isReallyContainer()) {
                long lockId = target.getLockId();
                Item lock;
                if (lockId == -10L) {
                    lock = null;
                } else {
                    try {
                        lock = Items.getItem(lockId);
                    } catch (NoSuchItemException e) {
                        lock = null;
                    }
                }

                if (lock == null) {
                    res.add(Actions.actionEntrys[Actions.SET_LOCK]);
                } else {
                    res.add(new ActionEntry(Actions.REPLACE, "Replace lock", "replacing lock"));
                }

                res = new ArrayList<>(res);
            }
        }

        return res;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        switch (OpenCloseCheck.getState(performer, target)) {
            case OPEN:
                return openList;
            case CLOSE:
                return closeList;
            default:
                return Collections.emptyList();
        }
    }
}
