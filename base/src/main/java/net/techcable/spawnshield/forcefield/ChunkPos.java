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


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

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
    public static ChunkPos fromChunk(Chunk chunk) {
        return new ChunkPos(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }
    public static ChunkPos fromLocation(Location l) {
        return new ChunkPos(l.getBlockX() >> 4, l.getBlockZ() >> 4, l.getWorld());
    }
    public static int toRelative(int absolute) {
        return absolute & 0xF; //First 16 bits
    }
    public static int fromRelative(int chunk, int relative) {
        return (chunk << 4) | (relative & 0xF);
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(getX(), getZ());
    }
}
