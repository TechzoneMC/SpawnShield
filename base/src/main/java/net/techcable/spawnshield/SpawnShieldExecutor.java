/**
 * The MIT License
 * Copyright (c) 2015 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.techcable.spawnshield;

import lombok.*;

import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class SpawnShieldExecutor implements CommandExecutor {
    private final SpawnShield plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Insufficient arguments");
            printHelp(sender);
            return true;
        }
        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("info")) {
            printInfo(sender);
            return true;
        } else if (subCommand.equalsIgnoreCase("help")) {
            printHelp(sender);
            return true;
        } else if (subCommand.equalsIgnoreCase("block")) {
            if (!sender.hasPermission("spawnshield.block")) {
                sender.sendMessage("You don't have permission to edit the block list");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Insufficient arguments");
                printHelp(sender);
                return true;
            }
            String subSubCommand = args[1];
            if (subSubCommand.equalsIgnoreCase("add")) {
                if (args.length < 4) {
                    sender.sendMessage("Insufficient arguments");
                    printHelp(sender);
                    return true;
                }
                String regionName = args[2];
                String worldName = args[3];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage("World " + worldName + " is not known");
                    return true;
                }
                boolean hasRegion = false;
                for (ProtectionPlugin protectionPlugin : plugin.getSettings().getPlugins()) {
                    if (!protectionPlugin.hasRegion(world, regionName)) continue;;
                    hasRegion = true;
                    Region region = protectionPlugin.getRegion(world, regionName);
                    plugin.getSettings().addRegionToBlock(region);
                    sender.sendMessage("Successfuly blocked region " + regionName + " in " + worldName);
                }
                if (!hasRegion) {
                    sender.sendMessage("Region not found");
                }
                return true;
            } else if (subSubCommand.equalsIgnoreCase("remove")) {
                if (args.length < 4) {
                    sender.sendMessage("Insufficient arguments");
                    printHelp(sender);
                    return true;
                }
                String regionName = args[2];
                String worldName = args[3];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage("World " + worldName + " is not known");
                    return true;
                }
                boolean hasRegion = false;
                for (ProtectionPlugin protectionPlugin : plugin.getSettings().getPlugins()) {
                    if (!protectionPlugin.hasRegion(world, regionName)) continue;;
                    hasRegion = true;
                    Region region = protectionPlugin.getRegion(world, regionName);
                    plugin.getSettings().addRegionToBlock(region);
                }
                if (hasRegion) {
                    sender.sendMessage("Successfuly unblocked region " + regionName + " in " + worldName);
                } else {
                    sender.sendMessage("Unknown region");
                }
                return true;
            } else if (subSubCommand.equalsIgnoreCase("list")) {
                sender.sendMessage(color("&b Blocked Regions"));
                for (Region region : plugin.getSettings().getRegionsToBlock()) {
                    sender.sendMessage("&7Region&r " + region.getName() + " &7in world&r " + region.getWorld().getName());
                }
                return true;
            } else {
                sender.sendMessage(subSubCommand + " is not a valid sub command of /spawnshield block");
                printHelp(sender);
                return true;
            }
        } else {
            sender.sendMessage(subCommand + " is not a valid sub command of /spawnshield");
            return true;
        }
    }

    public void printInfo(CommandSender sender) {
        sender.sendMessage("[SpawnShield] This plugin prevents people from entering a safezone in combat");
        sender.sendMessage("[SpawnShield] This plugin was created by Techcable, and is available for free on spigotmc");
    }

    public void printHelp(CommandSender sender) {
        sender.sendMessage(color("&9--------&2 SpawnShield Help &9--------"));
        sender.sendMessage(color("&9/spawnshield info &0-- &9Displays information about the plugin"));
        sender.sendMessage(color("&9/spawnshield help &0-- &9Displays this help message"));
        sender.sendMessage(color("&9/spawnshield block add [region] [world] &0-- &9Adds a region to the blocked region list"));
        sender.sendMessage(color("&9/spawnshield block remove [region] [world] &0-- &9Removes a region from the blocked region list"));
        sender.sendMessage(color("&9/spawnshield block list &0-- &9Lists blocked regions"));
        sender.sendMessage(color("&9--------&2 SpawnShield Help &9--------"));
    }

    private static String color(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
