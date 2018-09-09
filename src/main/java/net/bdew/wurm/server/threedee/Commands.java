package net.bdew.wurm.server.threedee;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modloader.interfaces.MessagePolicy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
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
                    ContainerEntry toAdd;
                    if (existing == null) {
                        toAdd = new ContainerEntry(idToAdd, x, y, z, xo, yo);
                        toAdd.overrideTemplateFlags();
                    } else {
                        toAdd = existing.resized(x, y, z, xo, yo);
                    }
                    ThreeDeeMod.containers.put(idToAdd, toAdd);
                    communicator.sendAlertServerMessage(String.format("Added %s to surface list", toAdd.getTemplate().getName()));
                    break;
                case "del":
                    int idToDel = Integer.parseInt(parser.nextToken());
                    ContainerEntry ent = ThreeDeeMod.containers.remove(idToDel);
                    if (ent != null) {
                        ent.restoreTemplateFlags();
                        communicator.sendAlertServerMessage(String.format("Removed %s from surface list", ent.getTemplate().getName()));
                    } else communicator.sendAlertServerMessage("Item is not in surface list");
                    break;
                case "save":
                    try (PrintStream fs = new PrintStream("mods/threedee.config")) {
                        fs.println("# If set to higher than 0 - only GMs with that power level will be able to place items");
                        fs.println(String.format(Locale.US, "minPower=%d", ThreeDeeMod.minPower));
                        fs.println("#=====================================================================================");
                        fs.println("# Container settings");
                        fs.println("# Syntax: container@<templateId>=<SizeX>,<SizeY>,<SizeZ>,<OffsetX>,<OffsetY>");
                        fs.println("# All values in meters");
                        ArrayList<ContainerEntry> list = new ArrayList<>(ThreeDeeMod.containers.values());
                        list.sort(Comparator.comparingInt(o -> o.templateId));
                        for (ContainerEntry c : list) {
                            ItemTemplate tpl = c.getTemplate();
                            fs.printf("# %s%s (%s)%n", tpl.sizeString, tpl.getName(), tpl.isWood() ? "wood" : Materials.convertMaterialByteIntoString(tpl.getMaterial()));
                            fs.println(String.format(Locale.US, "container@%d=%f,%f,%f,%f,%f", c.templateId, c.sizeX, c.sizeY, c.sizeZ, c.xOffset, c.yOffset));
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
