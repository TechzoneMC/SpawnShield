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
package net.techcable.spawnshield.compat;


import lombok.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Throwables;

/**
 * An immutable position of a chunk
 */
@RequiredArgsConstructor
@Getter
public class ChunkPos {

    private final int x, z;
    private final World world;

    public int getAbsoluteX(int relativeX) {
        return fromRelative(getX(), relativeX);
    }

    public int getAbsoluteZ(int absoluteZ) {
        return fromRelative(getZ(), absoluteZ);
    }

    public ChunkPos(Chunk chunk) {
        this(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }

    public ChunkPos(Location l) {
        this(l.getBlockX() >> 4, l.getBlockZ() >> 4, l.getWorld());
    }

    public int getRelativeX(BlockPos absolute) {
        return absolute.getX() & 0xF; //First 16 bits
    }

    public int getRelativeZ(BlockPos absolute) {
        return absolute.getZ() & 0xF; //First 16 bits
    }

    public int getSection(BlockPos absolute) {
        return absolute.getY() & 0xF;
    }

    private static int fromRelative(int chunk, int relative) {
        return (chunk << 4) | (relative & 0xF);
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(getX(), getZ());
    }

    public void load() {
        if (isLoaded()) return;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SpawnShield");
        if (Bukkit.isPrimaryThread()) {
            getWorld().loadChunk(getX(), getZ(), true);
            return;
        }
        Future<Void> future = Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                load();
                return null;
            }
        });
        while (true) {
            try {
                future.get();
                break;
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                Throwables.propagate(e);
            }
        }
    }

}
