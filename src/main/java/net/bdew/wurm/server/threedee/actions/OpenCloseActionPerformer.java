package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;

public class OpenCloseActionPerformer implements ActionPerformer {
    private final short actionId;

    public OpenCloseActionPerformer(short actionId) {
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
        if (OpenCloseCheck.getState(performer, target) == OpenCloseCheck.NOTHING)
            return propagate(action, ActionPropagation.SERVER_PROPAGATION, ActionPropagation.ACTION_PERFORMER_PROPAGATION);

        if (actionId == Actions.OPEN) {
            if (performer.addItemWatched(target)) {
                if (target.getDescription().isEmpty()) {
                    performer.getCommunicator().sendOpenInventoryWindow(target.getWurmId(), target.getName());
                } else {
                    performer.getCommunicator().sendOpenInventoryWindow(target.getWurmId(), target.getName() + " [" + target.getDescription() + "]");
                }
                target.addWatcher(target.getWurmId(), performer);
                target.sendContainedItems(target.getWurmId(), performer);
            }
        } else {
            if (performer.getCommunicator().sendCloseInventoryWindow(target.getWurmId())) {
                target.removeWatcher(performer, true);
            }
        }

        return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
    }
}
