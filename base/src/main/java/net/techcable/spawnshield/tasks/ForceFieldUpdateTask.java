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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;
import net.techcable.spawnshield.forcefield.BorderFinder;
import net.techcable.spawnshield.forcefield.ForceFieldUpdateRequest;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class ForceFieldUpdateTask extends BukkitRunnable {

    public void request(ForceFieldUpdateRequest request) {
        requests.put(request.getPlayer().getId(), request);
    }

    public void clearRequest(UUID id) {
        requests.remove(id);
    }

    private final ConcurrentMap<UUID, ForceFieldUpdateRequest> requests = new ConcurrentHashMap<>();
    private final ListeningExecutorService executor = makeExecutor();

    private ListeningExecutorService makeExecutor() {
        if (Runtime.getRuntime().availableProcessors() == 1) {
            return MoreExecutors.sameThreadExecutor();
        } else {
            int numExecutors = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);
            return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(numExecutors, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "SpawnShield Forcefield Worker");
                }
            }));
        }
    }

    private volatile int numExecuting = 0;
    @Override
    public void run() {
        for (UUID playerId : requests.keySet()) {
            final ForceFieldUpdateRequest request = requests.get(playerId);
            if (request == null) continue;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    numExecuting++;
                    try {
                        runRequest(request);
                    } finally {
                        numExecuting--;
                    }
                }
            });
        }
    }

    public void runRequest(ForceFieldUpdateRequest request) {
        Set<BlockPos> shownBlocks = new HashSet<>();
        BlockPos center = request.getPosition();
        int radius = request.getUpdateRadius();
        for (Region region : request.getRegionsToUpdate()) {
            for (BlockPos borderPoint : getBorders(region)) {
                if (isInsideCircle(center, borderPoint, radius)) {
                    shownBlocks.add(borderPoint);
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
            request.getPlayerEntity().sendBlockChange(toShow.toLocation(), Material.STAINED_GLASS, (byte) 14);
        }
        request.getPlayer().setLastShownBlocks(shownBlocks);
    }

    private final Map<Region, Collection<BlockPos>> borderCache = Maps.newHashMap(); //Will only be accessed by a single task, so no need for synchronization

    private Collection<BlockPos> getBorders(Region region) {
        if (borderCache.size() > 50) {
            Utils.severe("Cache exceeded 50 entries, which should never happen.");
            Utils.severe("Clearing cache");
            borderCache.clear();
        }
        if (!borderCache.containsKey(region)) {
            borderCache.put(region, BorderFinder.getBorderPoints(region));
        }
        return borderCache.get(region);
    }

    private static boolean isInsideCircle(BlockPos center, BlockPos toCheck, int radius) {
        int centerX = center.getX();
        int centerZ = center.getZ();
        int x = toCheck.getX();
        int z = toCheck.getZ();
        return square(x - centerX) + square(z - centerZ) <= square(radius);
    }

    private static int square(int i) {
        return i * i;
    }
}
