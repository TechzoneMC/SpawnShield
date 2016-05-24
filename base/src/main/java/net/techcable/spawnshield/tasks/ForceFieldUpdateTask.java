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

import com.google.common.collect.*;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.RequiredArgsConstructor;

import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;
import net.techcable.spawnshield.forcefield.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class ForceFieldUpdateTask extends AbstractFuture implements Runnable, ListenableFuture {

    public static ListenableFuture<?> schedule(ForceFieldUpdateRequest request) {
        ForceFieldUpdateTask task = new ForceFieldUpdateTask(request);
        Bukkit.getScheduler().runTask(SpawnShield.getInstance(), task);
        return task;
    }
    private final ForceFieldUpdateRequest request;
    @Override
    public void run() {
        Set<BlockPos> shownBlocks = new HashSet<BlockPos>();
        for (Region region : request.getRegionsToUpdate()) {
            for (BlockPos borderPoint : getBorders(region)) {
                for (int y = region.getMin().getY(); y <= region.getMax().getY(); y++) {
                    BlockPos toShow = borderPoint.withY(y);
                    int distance = toShow.distanceSquared(request.getPosition());
                    if (distance <= request.getUpdateRadius()) {
                        shownBlocks.add(toShow);
                    }
                }
            }
        }
        Collection<BlockPos> lastShown = request.getPlayer().getLastShownBlocks();
        if (lastShown == null) lastShown = new HashSet<>();
        for (BlockPos noLongerShown : lastShown) {
            if (shownBlocks.contains(noLongerShown)) continue; //We will show
            request.getPlayerEntity().sendBlockChange(noLongerShown.toLocation(), noLongerShown.getTypeAt().getId(), noLongerShown.getDataAt());
        }
        for (BlockPos toShow : shownBlocks) {
            if (toShow.getTypeAt().isSolid()) continue;
            request.getPlayerEntity().sendBlockChange(toShow.toLocation(), Material.STAINED_GLASS, (byte)14);
        }
        request.getPlayer().setLastShownBlocks(shownBlocks);
        set(null);
    }

    private final Map<Region, Collection<BlockPos>> borderCache = Maps.newHashMap(); //Will only be accessed by a single task, so no need for synchronization
    private Collection<BlockPos> getBorders(Region region) {
        if (borderCache.size() > 50) {
            SpawnShield.getInstance().getLogger().severe("Cache exceeded 50 entries, which should never happen.");
            SpawnShield.getInstance().getLogger().severe("Clearing cache");
            borderCache.clear();
        }
        if (!borderCache.containsKey(region)) {
            borderCache.put(region, BorderFinder.getBorderPoints(region));
        }
        return borderCache.get(region);
    }
}
