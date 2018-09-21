package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import net.bdew.wurm.server.threedee.Utils;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MoveBehaviourProvider implements BehaviourProvider {

    private final List<ActionEntry> submenuFull, submenuTurn;

    public MoveBehaviourProvider() {
        submenuFull = new LinkedList<>();
        submenuFull.add(new ActionEntry((short) (-7), "Move placement", "Move placement"));
        submenuFull.add(Actions.actionEntrys[Actions.TURN_ITEM]);
        submenuFull.add(Actions.actionEntrys[Actions.TURN_ITEM_BACK]);
        submenuFull.add(Actions.actionEntrys[Actions.PUSH]);
        submenuFull.add(Actions.actionEntrys[Actions.PUSH_GENTLY]);
        submenuFull.add(Actions.actionEntrys[Actions.PULL]);
        submenuFull.add(Actions.actionEntrys[Actions.PULL_GENTLY]);
        submenuFull.add(Actions.actionEntrys[Actions.MOVE_CENTER]);

        submenuTurn = new LinkedList<>();
        submenuTurn.add(new ActionEntry((short) (-2), "Move placement", "Move placement"));
        submenuTurn.add(Actions.actionEntrys[Actions.TURN_ITEM]);
        submenuTurn.add(Actions.actionEntrys[Actions.TURN_ITEM_BACK]);
    }

    static boolean canUse(Creature performer, Item target, boolean turn) {
        Item top = Utils.getSurface(target);
        if (top == null) return false;
        if (!performer.isPlayer() || !Utils.canAccessPlacedItem(target, performer)) return false;
        if (top.isLocked()) return true; // already checked in canAccessPlacedItem
        VolaTile tile = Zones.getTileOrNull(top.getTilePos(), top.isOnSurface());
        if (tile != null && tile.getVillage() != null && !tile.getVillage().getRoleFor(performer).mayPushPullTurn())
            return false;
        return turn || !Utils.isManualOnly(top);
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        if (canUse(performer, target, false))
            return submenuFull;
        else if (canUse(performer, target, true))
            return submenuTurn;
        else
            return Collections.emptyList();
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        return getBehavioursFor(performer, target);
    }
}
