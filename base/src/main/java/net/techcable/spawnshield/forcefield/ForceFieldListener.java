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
package net.techcable.spawnshield.forcefield;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import net.techcable.spawnshield.CombatAPI;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class ForceFieldListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().equals(event.getTo())) return; //Don't wanna fire if the player turned his head
        final SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(event.getPlayer());
        if (!CombatAPI.isTagged(event.getPlayer())) {
            SpawnShield.getInstance().clearRequest(event.getPlayer().getUniqueId());
            if (player.getLastShownBlocks() != null) {
                if (player.getLastShownBlocks() != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getLastShownBlocks() == null) return;
                            for (BlockPos lastShown : player.getLastShownBlocks()) {
                                player.getEntity().sendBlockChange(lastShown.toLocation(), lastShown.getTypeAt(), lastShown.getDataAt());
                            }
                            player.setLastShownBlocks(null);
                        }
                    }.runTaskAsynchronously(SpawnShield.getInstance());
                }
                return;
            }
            return;
        }
        BlockPos pos = new BlockPos(player.getEntity().getLocation());
        Collection<Region> toUpdate = new HashSet<>();
        for (Region region : SpawnShield.getInstance().getSettings().getRegionsToBlock()) {
            if (!region.getWorld().equals(event.getPlayer().getWorld())) continue; //We dont need this one: Yay!
            toUpdate.add(region);
        }
        ForceFieldUpdateRequest request = new ForceFieldUpdateRequest(pos, toUpdate, player, SpawnShield.getInstance().getSettings().getForcefieldRange());
        SpawnShield.getInstance().request(request);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        SpawnShield.getInstance().clearRequest(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        SpawnShield.getInstance().clearRequest(e.getPlayer().getUniqueId());
    }
}
