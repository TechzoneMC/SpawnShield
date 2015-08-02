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
package net.techcable.spawnshield.tasks;

import net.techcable.spawnshield.CombatAPI;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.Region;
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
                if (player.isBlocked()){
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
        for (Region r : SpawnShield.getInstance().getSettings().getRegionsToBlock()) {
            if (r.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ())) return true;
        }
        return false;
    }
}
