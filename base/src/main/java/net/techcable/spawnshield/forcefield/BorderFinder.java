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
package net.techcable.spawnshield.forcefield; /**
 * (c) 2015 Nicholas Schlabach
 * 
 * You are free to use this class as long as you meet the following conditions
 * 
 * 1. You may not charge money for access to any software conaining or accessing this class
 * 2. Any software containing or accessing this class must be available to the public on either dev.bukkit.org or spigotmc.org
 * 3. ALL source code for any piece of software using this class must be given out
 */
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.Region;

import java.util.Collection;
import java.util.HashSet;

public class BorderFinder {
    private BorderFinder() {}

    public static Collection<BlockPos> getBorderPoints(Region region) {
        HashSet<BlockPos> result = new HashSet<>();
        for (BlockPos point : region.getPoints()) {
            getAlongX(point, region, result);
            getAlongZ(point, region, result);
        }
        getBottomOrTop(region.getMin(), region, result);
        getBottomOrTop(region.getMax(), region, result);
        return result;
    }

    private static void getBottomOrTop(BlockPos bottomOrTop, Region region, HashSet<BlockPos> result) {
        for (int x = bottomOrTop.getX(); region.contains(x, bottomOrTop.getY(), bottomOrTop.getZ()); x++) {
            for (int z = bottomOrTop.getZ(); region.contains(x, bottomOrTop.getY(), z); z++) {
                result.add(new BlockPos(x, bottomOrTop.getY(), z, region.getWorld()));
            }
        }
    }

    private static void getAlongX(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX() + 1, start.getY(), start.getZ())) { //We are positive
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x++) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        } else { //We are negative or one block
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x--) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        }
    }
    
    private static void getAlongZ(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX(), start.getY(), start.getZ() + 1)) { //We are positive
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z++) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        } else { //We are negative or one block
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z--) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        }
    }
}