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
package net.techcable.spawnshield.compat.worldguard6;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

import java.lang.ref.WeakReference;
import java.util.*;

import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;

@Getter
public class ProtectedRegionRegion implements Region {
    @NonNull
    private final WeakReference<ProtectedRegion> handle;
    @NonNull
    private final World world;

    public ProtectedRegion getHandle() {
        return handle.get();
    }

    public ProtectedRegionRegion(ProtectedRegion handle, World world) {
        this.handle = new WeakReference<ProtectedRegion>(handle);
        this.world = world;
    }

    @Override
    public boolean contains(BlockPos point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return getHandle().contains(x, y, z);
    }

    @Override
    public Collection<BlockPos> getPoints() {
        List<BlockVector2D> rawPoints = getHandle().getPoints();
        Set<BlockPos> points = new HashSet<>();
        int y = getHandle().getMinimumPoint().getBlockY(); //Works for me :)
        for (BlockVector2D rawPoint : rawPoints) {
            points.add(new BlockPos(rawPoint.getBlockX(), y, rawPoint.getBlockZ(), world));
        }
        return points;
    }

    @Override
    public BlockPos getMin() {
        BlockVector min = getHandle().getMinimumPoint();
        return new BlockPos(min.getBlockX(), min.getBlockY(), min.getBlockZ(), getWorld());
    }


    @Override
    public BlockPos getMax() {
        BlockVector max = getHandle().getMaximumPoint();
        return new BlockPos(max.getBlockX(), max.getBlockY(), max.getBlockZ(), getWorld());
    }

    @Override
    public String getName() {
        return getHandle().getId();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = prime;
        result = prime * result + world.hashCode();
        result = prime * result + handle.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == null) return false;
        if (otherObj == this) return true;
        if (otherObj instanceof ProtectedRegionRegion) {
            ProtectedRegionRegion other = (ProtectedRegionRegion) otherObj;
            if (!getWorld().equals(other.getWorld())) return false;
            if (!getHandle().equals(other.getHandle())) return false;
            return true;
        }
        return false;
    }
}
