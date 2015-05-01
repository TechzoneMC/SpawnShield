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
package net.techcable.spawnshield.forcefield;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.techcable.spawnshield.CombatAPI;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import net.techcable.techutils.TechScheduler;
import net.techcable.techutils.collect.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class ForceFieldListener implements Listener {
    private final Set<UUID> currentlyProcessing = Sets.newSetFromMap(Maps.<UUID, Boolean>newConcurrentMap());
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().equals(event.getTo())) return; //Don't wanna fire if the player turned his head
        if (currentlyProcessing.contains(event.getPlayer().getUniqueId())) return;
        final SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(event.getPlayer());
        if (!CombatAPI.isTagged(event.getPlayer())) {
            if (player.getLastShownBlocks() != null && !currentlyProcessing.contains(player.getId())) {
                currentlyProcessing.add(player.getId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (BlockPos lastShown : player.getLastShownBlocks()) {
                            player.getEntity().sendBlockChange(lastShown.toLocation(), lastShown.getTypeAt(), lastShown.getDataAt());
                        }
                        player.setLastShownBlocks(null);
                        currentlyProcessing.remove(player.getId());
                    }
                }.runTaskAsynchronously(SpawnShield.getInstance());
            }
            return;
        }
        currentlyProcessing.add(player.getId());
        BlockPos pos = new BlockPos(player.getEntity().getLocation());
        Collection<Region> toUpdate = new HashSet<>();
        for (Pair<World, ProtectedRegion> wgRegion : SpawnShield.getInstance().getSettings().getRegionsToBlock()) {
            if (!wgRegion.getFirst().equals(event.getPlayer().getWorld())) continue; //We dont need this one: Yay!
            ProtectedRegionRegion region = new ProtectedRegionRegion(wgRegion.getSecond(), wgRegion.getFirst());
            toUpdate.add(region);
        }
        ForceFieldUpdateRequest request = new ForceFieldUpdateRequest(pos, toUpdate, player, SpawnShield.getInstance().getSettings().getForcefieldRange());
        final ForceFieldUpdateTask task = new ForceFieldUpdateTask(request);
        Bukkit.getScheduler().runTaskAsynchronously(SpawnShield.getInstance(), task);
        task.addListener(new Runnable() {
            @Override
            public void run() {
                currentlyProcessing.remove(player.getId());
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        currentlyProcessing.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        currentlyProcessing.remove(e.getPlayer().getUniqueId());
    }
}
