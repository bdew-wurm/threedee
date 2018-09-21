package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import net.bdew.wurm.server.threedee.Utils;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.Collections;
import java.util.List;

public class PlaceAction implements ModAction, ActionPerformer, BehaviourProvider {
    private final ActionEntry actionEntry;

    public PlaceAction() {
        actionEntry = ActionEntry.createEntry((short) ModActions.getNextActionId(), "Put on top", "putting", new int[]{
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return this;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        if (Utils.canPlaceOnSurface(performer, source, target) && !Utils.isManualOnly(target))
            return Collections.singletonList(actionEntry);
        else
            return null;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        try {
            if (Utils.canPlaceOnSurface(performer, source, target) && !Utils.isManualOnly(target))
                Utils.doPlaceOnSurface(source, target, performer);
            else
                performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that.");
        } catch (FailedException | NoSuchTemplateException | NoSuchItemException e) {
            ThreeDeeMod.logException("Error placing item", e);
            performer.getCommunicator().sendAlertServerMessage("Placing failed, try again later or contact staff.");
        }
        return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
    }
}
