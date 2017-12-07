package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import net.bdew.wurm.server.threedee.Hooks;
import net.bdew.wurm.server.threedee.ThreeDeeMod;
import net.bdew.wurm.server.threedee.ThreeDeeStuff;
import net.bdew.wurm.server.threedee.Util3D;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.Collections;
import java.util.List;

public class PlaceAction implements ModAction, ActionPerformer, BehaviourProvider {
    private final ActionEntry actionEntry;

    public PlaceAction() {
        actionEntry = ActionEntry.createEntry((short) ModActions.getNextActionId(), "Place", "placing", new int[]{
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

    private boolean canUse(Creature performer, Item source, Item target) {
        return (performer.isPlayer()) &&
                (source != null) && (source.getTopParentOrNull() == performer.getInventory()) && (source.canBeDropped(true)) &&
                (target != null) && (ThreeDeeMod.containers.containsKey(target.getTemplateId()) && (target.getParentId() == -10));
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        if (canUse(performer, source, target))
            return Collections.singletonList(actionEntry);
        else
            return null;
    }

    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        try {
            Item hook = ItemFactory.createItem(ThreeDeeStuff.hookItemId, 99f, null);
            source.getParent().dropItem(source.getWurmId(), false);
            hook.insertItem(source, true, false);
            target.insertItem(hook, true, false);
            Util3D.forAllWatchers(target, player -> Hooks.sendItemHook(player.getCommunicator(), target));
        } catch (FailedException | NoSuchTemplateException | NoSuchItemException e) {
            ThreeDeeMod.logException("Error placing item", e);
            performer.getCommunicator().sendAlertServerMessage("Placing failed, try again later or contact staff.");
        }
        return propagate(action, ActionPropagation.NO_SERVER_PROPAGATION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.FINISH_ACTION);
    }
}
