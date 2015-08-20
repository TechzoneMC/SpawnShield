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
package net.techcable.spawnshield;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java8.util.stream.StreamSupport;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import net.techcable.spawnshield.config.SpawnShieldConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java8.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java8.util.stream.Collectors;

public class RegionManager {
    private final Set<Region> regions = Sets.newSetFromMap(new ConcurrentHashMap<>());

    public void refresh(SpawnShieldConfig config) {
        Utils.assertMainThread();
        refresh(config.getBlockRegions());
    }

    public void refresh(Collection<String> regionNames) {
        Set<Region> regions = StreamSupport.stream(regionNames).
                map(this::createRegion)
                .filter((r) -> r != null)
                .collect(Collectors.toSet());
        this.regions.clear();
        this.regions.addAll(regions);
    }

    public void saveConfig(SpawnShieldConfig config) {
        Utils.assertMainThread();
        config.getBlockRegions().clear();
        config.getBlockRegions().addAll(getNames());
    }

    public void removeRegionToBlock(Region region) {
        regions.remove(region);
    }


    public void addRegionToBlock(Region region) {
        regions.add(region);
    }

    /**
     * Return the last known names of the regions in this map
     * <p>
     * Will return the latest if all calls to {@link #refresh(Collection)} are finished, and none are started till this finishes.
     *
     * @return the last known names of the regions in this map
     */
    public ImmutableCollection<String> getNames() {
        return StreamSupport.stream(regions).map(RegionManager::getName).collect(Utils.toImmutableSet());
    }

    public boolean isBlocked(BlockPos pos) {
        for (Region region : regions) {
            if (region.contains(pos)) return true;
        }
        return false;
    }

    private final Set<ProtectionPlugin> plugins = Sets.newSetFromMap(new ConcurrentHashMap<>());

    public void addProtectionPlugin(ProtectionPlugin plugin) {
        plugins.add(plugin);
    }

    public static final Pattern NAME_PATTERN = Pattern.compile("([^:]*)(?::(.*))?"); // (region name)(?::(world name))?

    public boolean hasRegion(String regionName, World world) {
        for (ProtectionPlugin plugin : plugins) {
            if (plugin.hasRegion(world, regionName)) return true;
        }
        return false;
    }

    public Region getRegion(String regionName, World world) {
        for (ProtectionPlugin plugin : plugins) {
            if (!plugin.hasRegion(world, regionName)) continue;
            return plugin.getRegion(world, regionName);
        }
        return null;
    }

    public static String getName(Region region, String oldName) {
        return getName(region); // the old name is ignored
    }

    public static String getName(Region region) {
        return region.getName() + ":" + region.getWorld().getName();
    }

    public ImmutableSet<Region> getBlockedRegions() {
        return ImmutableSet.copyOf(regions);
    }

    public void forEach(Consumer<Region> region) {
        StreamSupport.stream(regions).forEach(region);
    }

    private Region createRegion(String name) {
        Matcher m = NAME_PATTERN.matcher(name);
        Preconditions.checkArgument(m.matches(), "Invalid region name: " + name);
        String regionName = m.group(1);
        String worldName = m.group(2);
        World world = worldName != null ? Bukkit.getWorld(worldName) : null;
        for (ProtectionPlugin plugin : plugins) {
            if (world == null && plugin.isWorldSpecific()) continue;
            if (plugin.hasRegion(world, regionName)) {
                return plugin.getRegion(world, regionName);
            }
        }
        return null;
    }
}
