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

import com.google.common.base.Preconditions;
import lombok.experimental.Wither;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import lombok.*;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"x", "y", "z", "world"})
public class BlockPos {

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

    public Material getTypeAt() {
        return Material.getMaterial(getWorld().getBlockTypeIdAt(getX(), getY(), getZ()));
    }

    public byte getDataAt() {
        return getWorld().getBlockAt(getX(), getY(), getZ()).getData();
    }

    @Getter(lazy = true)
    private final ChunkPos chunkPos = new ChunkPos(getX() >> 4, getZ() >> 4, getWorld());

    private static int square(int i) {
        return i * i;
    }
}
