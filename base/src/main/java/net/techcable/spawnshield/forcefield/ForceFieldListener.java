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

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.change.BlockChangeTracker;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;
import net.techcable.spawnshield.nms.ChunkNotLoadedException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
public class ForceFieldListener implements Listener {
    private final SpawnShield plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().equals(event.getTo())) return; //Don't wanna fire if the player turned his head
        final SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(event.getPlayer());
        if (!player.isBlocked()) {
            SpawnShield.getInstance().clearRequest(event.getPlayer().getUniqueId());
            if (player.getLastShownBlocks() != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getLastShownBlocks() == null) return;
                        BlockChangeTracker tracker = new BlockChangeTracker(player.getEntity());
                        for (BlockPos lastShown : player.getLastShownBlocks()) {
                            try {
                                tracker.addBlockChange(lastShown, lastShown.getTypeAt(), lastShown.getDataAt());
                            } catch (ChunkNotLoadedException e) {
                                continue; // We don't need to refresh blocks the player can't see
                            }
                        }
                        tracker.flush();
                        player.setLastShownBlocks(null);
                    }
                }.runTaskAsynchronously(SpawnShield.getInstance());
            }
            return;
        }
        BlockPos pos = new BlockPos(player.getEntity().getLocation());
        ImmutableSet<Region> toUpdate = plugin.getRegionManager().getBlockedRegions();
        ForceFieldUpdateRequest request = new ForceFieldUpdateRequest(pos, toUpdate, player, SpawnShield.getInstance().getSettings().getForceFieldRange());
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
