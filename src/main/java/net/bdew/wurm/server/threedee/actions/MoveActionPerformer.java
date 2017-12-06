package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;

import static org.gotti.wurmunlimited.modsupport.actions.ActionPropagation.ACTION_PERFORMER_PROPAGATION;
import static org.gotti.wurmunlimited.modsupport.actions.ActionPropagation.SERVER_PROPAGATION;

public class MoveActionPerformer implements ActionPerformer {
    private final short actionId;

    public MoveActionPerformer(short actionId) {
        this.actionId = actionId;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        if (MoveBehaviourProvider.canUse(performer, target)) {
            ThreeDeeMod.logInfo(String.format("Move action %d on %s", action.getNumber(), target));
            return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
        } else {
            return propagate(action, SERVER_PROPAGATION, ACTION_PERFORMER_PROPAGATION);
        }
    }

    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }

}
