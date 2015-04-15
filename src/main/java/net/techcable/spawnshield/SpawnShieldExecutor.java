/**
 * Copyright (c) 2015 Nicholas Schlabach
 *
 * If there is conflict between two versions of this license, [the version found on gtihub](https://gist.github.com/Techcable/ea146b481870e3736b48) will take precedence.
 *
 * Permission is granted to operate this program provided that:
 * - You have purchased this plugin from Nicholas Schlabach (Techcable)
 * - Techcable has given you his express permission to operate/run this program
 * - Techcable has released this software publicly on either dev.bukkit.org, spigotmc.org or another software hosting site
 * - You do not modify this software in any way
 * - You do not redistrubute this software to anyone else
 * - You do not attempt to deobfuscate, decompile, or reverese engneer this plugin in any way
 * - Nicholas Schlabach (Techcable) reserves the right to change these terms an conditions at any time, with or without warning
 *
 * If any of the above terms are violated this plugin is no longer valid and you must stop using this software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.techcable.spawnshield;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.techcable.techutils.collect.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpawnShieldExecutor implements CommandExecutor {

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
                RegionManager manager = WGBukkit.getRegionManager(world);
                if (!manager.hasRegion(regionName)) {
                    sender.sendMessage(regionName + " is not a known region");
                    return true;
                }
                ProtectedRegion region = manager.getRegion(regionName);
                SpawnShield.getInstance().getSettings().addRegionToBlock(region);
                sender.sendMessage("Successfuly blocked region " + regionName + " in " + worldName);
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
                RegionManager manager = WGBukkit.getRegionManager(world);
                if (!manager.hasRegion(regionName)) {
                    sender.sendMessage(regionName + " is not a known region");
                    return true;
                }
                ProtectedRegion region = manager.getRegion(regionName);
                SpawnShield.getInstance().getSettings().removeRegionToBlock(region);
                sender.sendMessage("Successfuly unblocked region " + regionName + " in " + worldName);
                return true;
            } else if (subSubCommand.equalsIgnoreCase("list")) {
                sender.sendMessage(color("&b Blocked Regions"));
                for (Pair<World, ProtectedRegion> region : SpawnShield.getInstance().getSettings().getRegionsToBlock()) {
                    sender.sendMessage("&7Region&r " + region.getSecond().getId() + " &7in world&r " + region.getFirst().getName());
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
