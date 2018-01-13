package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.CustomItems;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;

public class PreventTakeActionPerformer implements ActionPerformer {
    private final short actionId;

    public PreventTakeActionPerformer(short actionId) {
        this.actionId = actionId;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        if (target.getParentId() == -10 && ThreeDeeMod.containers.containsKey(target.getTemplateId())) {
            for (Item i : target.getItems()) {
                if (i.getTemplateId() == CustomItems.hookItemId) {
                    performer.getCommunicator().sendAlertServerMessage(String.format("The %s has items placed on it, remove them first.", target.getName()));
                    return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
                }
            }
        }
        return propagate(action, ActionPropagation.SERVER_PROPAGATION, ActionPropagation.ACTION_PERFORMER_PROPAGATION);
    }
}
