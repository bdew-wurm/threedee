package net.bdew.wurm.server.threedee;

import com.wurmonline.server.creatures.Communicator;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.MessagePolicy;

import java.io.PrintStream;
import java.util.StringTokenizer;

public class Commands {
    static MessagePolicy handleSurface(String cmd, Communicator communicator) {
        try {
            StringTokenizer parser = new StringTokenizer(cmd);
            parser.nextToken();
            String op = parser.nextToken();
            switch (op) {
                case "add":
                    int idToAdd = Integer.parseInt(parser.nextToken());
                    float x = Float.parseFloat(parser.nextToken());
                    float y = Float.parseFloat(parser.nextToken());
                    float z = Float.parseFloat(parser.nextToken());
                    float xo = Float.parseFloat(parser.nextToken());
                    float yo = Float.parseFloat(parser.nextToken());
                    ContainerEntry existing = ThreeDeeMod.containers.get(idToAdd);
                    ContainerEntry toAdd = new ContainerEntry(idToAdd, x, y, z, xo, yo);
                    if (existing == null) {
                        if (toAdd.getTemplate().isHollow())
                            toAdd.reallyContainer = true;
                        else
                            ReflectionUtil.setPrivateField(toAdd.getTemplate(), ThreeDeeMod.hollow, true);
                    } else {
                        toAdd.reallyContainer = existing.reallyContainer;
                    }
                    ThreeDeeMod.containers.put(idToAdd, toAdd);
                    communicator.sendAlertServerMessage(String.format("Added %s to surface list", toAdd.getTemplate().getName()));
                    break;
                case "del":
                    int idToDel = Integer.parseInt(parser.nextToken());
                    ContainerEntry ent = ThreeDeeMod.containers.remove(idToDel);
                    if (ent != null) {
                        if (!ent.reallyContainer)
                            ReflectionUtil.setPrivateField(ent.getTemplate(), ThreeDeeMod.hollow, false);
                        communicator.sendAlertServerMessage(String.format("Removed %s from surface list", ent.getTemplate().getName()));
                    } else communicator.sendAlertServerMessage("Item is not in surface list");
                    break;
                case "save":
                    try (PrintStream fs = new PrintStream("mods/threedee.config")) {
                        fs.println("# If set to higher than 0 - only GMs with that power level will be able to place items");
                        fs.println(String.format("minPower=%d", ThreeDeeMod.minPower));
                        fs.println("#=====================================================================================");
                        fs.println("# Container settings");
                        fs.println("# Syntax: container@<templateId>=<SizeX>,<SizeY>,<SizeZ>,<OffsetX>,<OffsetY>");
                        fs.println("# All values in meters");
                        for (ContainerEntry c : ThreeDeeMod.containers.values()) {
                            fs.println("# " + c.getTemplate().getName());
                            fs.println(String.format("container@%d=%f,%f,%f,%f,%f", c.templateId, c.sizeX, c.sizeY, c.sizeZ, c.xOffset, c.yOffset));
                        }
                    }
                    communicator.sendAlertServerMessage("Modified config saved.");
                    break;
                default:
                    communicator.sendAlertServerMessage("Unknown subcommand: " + op);
            }
        } catch (Throwable e) {
            ThreeDeeMod.logException(String.format("Error handling command '%s'", cmd), e);
            communicator.sendAlertServerMessage("Error handling command: " + e.toString());
        }
        return MessagePolicy.DISCARD;
    }
}
