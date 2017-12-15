package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;

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
        return getBehavioursFor(performer, target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        switch (OpenCloseCheck.getState(performer, target)) {
            case OPEN:
                return openList;
            case CLOSE:
                return closeList;
            default:
                return null;
        }
    }
}
