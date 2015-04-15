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
package net.techcable.spawnshield.tasks;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.techcable.combattag.Utils;
import net.techcable.spawnshield.CombatAPI;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.config.SpawnShieldMessages;
import net.techcable.techutils.collect.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportSafezoningTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player playerEntity : Bukkit.getOnlinePlayers()) {
            SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(playerEntity);
            if (isBlocked(playerEntity.getLocation())) {
                if (CombatAPI.isTagged(playerEntity)){
                    if (player.getLastLocationOutsideSafezone() == null) {
                        Utils.warning(player.getName() + "'s last location outside safezone is unknown");
                    } else {
                        if (player.getLastCantEnterMessageTime() + 1500 < System.currentTimeMillis()) {
                            playerEntity.sendMessage(SpawnShieldMessages.getInstance().getCantEnterSafezone());
                            player.setLastCantEnterMessageTime(System.currentTimeMillis());
                        }
                        playerEntity.teleport(player.getLastLocationOutsideSafezone(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                }
            } else {
                player.setLastLocationOutsideSafezone(playerEntity.getLocation());
            }
        }
    }

    public boolean isBlocked(Location l) {
        for (Pair<World, ProtectedRegion> r : SpawnShield.getInstance().getSettings().getRegionsToBlock()) {
            if (r.getSecond().contains(l.getBlockX(), l.getBlockY(), l.getBlockZ())) return true;
        }
        return false;
    }
}
