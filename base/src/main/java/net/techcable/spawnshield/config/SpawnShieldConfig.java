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
package net.techcable.spawnshield.config;

import lombok.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.techcable.spawnshield.BlockMode;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import net.techcable.techutils.config.AnnotationConfig;
import net.techcable.techutils.config.Setting;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.google.common.collect.Sets;

@Getter
public class SpawnShieldConfig extends AnnotationConfig {

    public void addRegionToBlock(Region r) {
        Utils.assertMainThread();
        blockRegions.add(r.getName());
        refreshRegionsToBlock();
    }

    public void removeRegionToBlock(Region r) {
        Utils.assertMainThread();
        blockRegions.remove(r.getName());
        refreshRegionsToBlock();
    }

    @Setting("blockRegions")
    @Getter(AccessLevel.NONE) //Use the cached and thread safe version
    private List<String> blockRegions;

    @Setting("mode")
    private BlockMode mode;

    @Setting("debug")
    private boolean debug;

    @Setting("force-field.range")
    private int forceFieldRange;

    @Synchronized("lock")
    public void refreshRegionsToBlock() {
        this.cachedRegionsToBlock = null;
    }

    @Getter(AccessLevel.NONE)
    private transient Set<Region> cachedRegionsToBlock;

    @Synchronized("lock")
    public void addProtectionPlugin(ProtectionPlugin plugin) {
        plugins.add(plugin);
    }

    @Setting("afterCombatDelay")
    @Getter(AccessLevel.NONE)
    private int afterCombatDelay;

    /**
     * Get the delay in milliseconds players must wait after being tagged until they can re-enter spawn
     *
     * @return the delay in millesconds
     */
    public long getAfterCombatDelay() {
        return TimeUnit.MINUTES.toMillis(afterCombatDelay);
    }

    private final Set<ProtectionPlugin> plugins = new HashSet<>();
    /**
     * Why not let the @Synchronised annotation create the lock for me?
     * Because AFAIK, it isn't transient, causing it to be serialized to config
     */
    private final transient Object lock = new Object();

    public Collection<Region> getRegionsToBlock() { // A devious combination of double checked locking and lazy initialization
        if (cachedRegionsToBlock != null) return cachedRegionsToBlock;
        synchronized (lock) {
            if (cachedRegionsToBlock != null) return cachedRegionsToBlock;
            cachedRegionsToBlock = Sets.newSetFromMap(new ConcurrentHashMap<Region, Boolean>());
            Set<String> notFound = Sets.newHashSet(blockRegions);
            for (ProtectionPlugin plugin : plugins) {
                for (World world : Bukkit.getWorlds()) {
                    for (String regionName : blockRegions) {
                        if (!plugin.hasRegion(world, regionName)) continue;
                        Region region = plugin.getRegion(world, regionName);
                        notFound.remove(regionName); //We found it !!
                        cachedRegionsToBlock.add(region);
                    }
                }
            }
            //Warn if we couldn't find a worldguard region
            for (String regionName : notFound) {
                Utils.warning(regionName + " is not a known region");
            }
            return cachedRegionsToBlock;
        }
    }
}
