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

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Synchronized;
import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import org.bukkit.World;

import java.util.Map;
import java.util.WeakHashMap;

public class WorldGuard6Plugin implements ProtectionPlugin {
    private final Map<ProtectedRegion, ProtectedRegionRegion> regionMap = new WeakHashMap<>();
    @Override
    @Synchronized
    public Region getRegion(World world, String name) {
        RegionManager manager = WGBukkit.getRegionManager(world);
        ProtectedRegion rawRegion = manager.getRegion(name);
        if (rawRegion == null) return null;
        if (regionMap.containsKey(rawRegion)) {
            return regionMap.get(rawRegion);
        }
        ProtectedRegionRegion region = new ProtectedRegionRegion(rawRegion, world);
        regionMap.put(rawRegion, region);
        return region;
    }

    @Override
    public boolean hasRegion(World world, String name) {
        RegionManager manager = WGBukkit.getRegionManager(world);
        return manager.hasRegion(name);
    }
}
