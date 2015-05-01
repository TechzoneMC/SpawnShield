/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.techcable.spawnshield.CombatAPI;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.techutils.collect.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class KnockbackTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player playerEntity : Bukkit.getOnlinePlayers()) {
            SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(playerEntity);
            if (isBlocked(playerEntity.getLocation())) {
                if (!CombatAPI.isTagged(playerEntity)) continue;
                if (player.getLastLocationOutsideSafezone() == null) return;
                if (player.getLastCantEnterMessageTime() + 1500 < System.currentTimeMillis()) {
                    playerEntity.sendMessage(SpawnShield.getInstance().getMessages().getCantEnterSafezone());
                    player.setLastCantEnterMessageTime(System.currentTimeMillis());
                }
                Vector knockback = player.getLastLocationOutsideSafezone().toVector().subtract(playerEntity.getLocation().toVector());
                playerEntity.getLocation().setDirection(knockback);
                playerEntity.setVelocity(knockback);
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