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

    private final List<ActionEntry> submenu;

    public MoveBehaviourProvider() {
        submenu = new LinkedList<>();
        submenu.add(new ActionEntry((short) (-7), "Move placement", "Move placement"));
        submenu.add(Actions.actionEntrys[Actions.TURN_ITEM]);
        submenu.add(Actions.actionEntrys[Actions.TURN_ITEM_BACK]);
        submenu.add(Actions.actionEntrys[Actions.PUSH]);
        submenu.add(Actions.actionEntrys[Actions.PUSH_GENTLY]);
        submenu.add(Actions.actionEntrys[Actions.PULL]);
        submenu.add(Actions.actionEntrys[Actions.PULL_GENTLY]);
        submenu.add(Actions.actionEntrys[Actions.MOVE_CENTER]);
    }

    static boolean canUse(Creature performer, Item target) {
        if (!performer.isPlayer() || !Utils.canAccessPlacedItem(target, performer)) return false;
        Item parent = target.getParentOrNull();
        Item top = parent.getParentOrNull();
        if (top.isLocked()) return true; // already checked in canAccessPlacedItem
        VolaTile tile = Zones.getTileOrNull(top.getTilePos(), top.isOnSurface());
        return tile == null || tile.getVillage() == null || tile.getVillage().getRoleFor(performer).mayPushPullTurn();
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        if (canUse(performer, target))
            return submenu;
        else
            return Collections.emptyList();
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        return getBehavioursFor(performer, target);
    }


}
