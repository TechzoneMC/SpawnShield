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
import lombok.experimental.*;

import net.techcable.spawnshield.nms.ChunkNotLoadedException;
import net.techcable.spawnshield.nms.NMS;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.google.common.base.Preconditions;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"x", "y", "z", "world"})
public class BlockPos {

    @Setter
    private static NMS nmsImplementation;

    public BlockPos(Location l) {
        this(l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld());
    }

    @Wither
    private final int x, y, z;
    private final World world;

    public Location toLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    public int distanceSquared(BlockPos other) {
        Preconditions.checkArgument(other.getWorld().equals(getWorld()), "Can't compare the distances of different worlds");
        return square(x - other.x) + square(y - other.y) + square(z - other.z);
    }

    public Material getTypeAt() throws ChunkNotLoadedException {
        int blockId;
        if (nmsImplementation == null) {
            blockId = getBlock().getTypeId();
        } else {
            blockId = nmsImplementation.getDirectId(this);
        }
        return Material.getMaterial(blockId);
    }

    public byte getDataAt() throws ChunkNotLoadedException {
        int data;
        if (nmsImplementation == null) {
            data = getBlock().getData();
        } else {
            data = nmsImplementation.getDirectId(this);
        }
        return (byte) data;
    }

    private Block getBlock() throws ChunkNotLoadedException {
        Chunk chunk = getChunkPos().getChunkIfLoaded();
        if (chunk == null) throw new ChunkNotLoadedException();
        return chunk.getBlock(getRelativeX(), getY(), getRelativeZ());
    }

    @Getter(lazy = true)
    private final ChunkPos chunkPos = new ChunkPos(getX() >> 4, getZ() >> 4, getWorld());

    public int getRelativeX() {
        return getChunkPos().getRelativeX(this);
    }

    public int getRelativeZ() {
        return getChunkPos().getRelativeZ(this);
    }

    public int getSection() {
        return getChunkPos().getSection(this);
    }

    private static int square(int i) {
        return i * i;
    }
}
