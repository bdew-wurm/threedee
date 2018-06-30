package net.bdew.wurm.server.threedee.actions;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import net.bdew.wurm.server.threedee.*;
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
            try {
                Item hook = target.getParent();
                Item top = hook.getParent();

                if (!hook.getAuxBit(7))
                    Utils.convertHook(hook, target);

                PosData pos = PosData.from(target);

                ContainerEntry cont = ThreeDeeMod.containers.get(top.getTemplateId());

                switch (num) {
                    case Actions.TURN_ITEM:
                        pos.rot = MovementScheme.normalizeAngle(pos.rot + 22.5f);
                        break;
                    case Actions.TURN_ITEM_BACK:
                        pos.rot = MovementScheme.normalizeAngle(pos.rot - 22.5f);
                        break;
                    case Actions.MOVE_CENTER:
                        pos.x = 0;
                        pos.y = 0;
                        pos.rot = 0;
                        break;
                    default:
                        float rot = MovementScheme.normalizeAngle(top.getRotation() - performer.getStatus().getRotation());
                        double dx = -Math.sin(rot * Math.PI / 180f);
                        double dy = -Math.cos(rot * Math.PI / 180f);

                        float step = Math.min(cont.sizeX, cont.sizeY);
                        switch (num) {
                            case Actions.PUSH_GENTLY:
                                step *= 0.01f;
                                break;
                            case Actions.PUSH:
                                step *= 0.1f;
                                break;
                            case Actions.PULL_GENTLY:
                                step *= -0.01f;
                                break;
                            case Actions.PULL:
                                step *= -0.1f;
                                break;
                        }

                        pos.x += (float) (dx * step);
                        pos.y += (float) (dy * step);

                        if (pos.x > cont.sizeX / 2) pos.x = cont.sizeX / 2;
                        if (pos.y > cont.sizeY / 2) pos.y = cont.sizeY / 2;
                        if (pos.x < -cont.sizeX / 2) pos.x = -cont.sizeX / 2;
                        if (pos.y < -cont.sizeY / 2) pos.y = -cont.sizeY / 2;
                        break;
                }

                pos.saveToItem(target);

                Utils.forAllWatchers(top, player -> Hooks.sendItemHook(player.getCommunicator(), top));
                return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
            } catch (NoSuchItemException e) {
                ThreeDeeMod.logException("Failed to get pos data from hook", e);
                performer.getCommunicator().sendAlertServerMessage("Movement failed, try again later or contact staff.");
                return propagate(action, ActionPropagation.FINISH_ACTION, ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION, ActionPropagation.NO_SERVER_PROPAGATION);
            }
        } else {
            return propagate(action, SERVER_PROPAGATION, ACTION_PERFORMER_PROPAGATION);
        }
    }

    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }

}
