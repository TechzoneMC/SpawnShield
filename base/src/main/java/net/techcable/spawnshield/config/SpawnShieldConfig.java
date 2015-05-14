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
package net.techcable.spawnshield.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import net.techcable.spawnshield.BlockMode;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import net.techcable.techutils.collect.Pair;
import net.techcable.techutils.yamler.Comments;
import net.techcable.techutils.yamler.Config;
import net.techcable.techutils.yamler.InvalidConfigurationException;
import net.techcable.techutils.yamler.InvalidConverterException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SpawnShieldConfig extends Config {
    public SpawnShieldConfig(SpawnShield plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
        try {
            addConverter(BlockMode.BlockModeConverter.class);
        } catch (InvalidConverterException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @Synchronized("lock")
    public void init() {
        Utils.assertMainThread();
        try {
            super.init();
        } catch (InvalidConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @Synchronized("lock")
    public void load() {
        Utils.assertMainThread();
        try {
            super.load();
        } catch (InvalidConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @Synchronized("lock")
    public void save() {
        Utils.assertMainThread();
        try {
            super.save();
        } catch (InvalidConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

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

    @Comments({
            "The list of regions to block entry into in combat",
            "This option can be controlled by the command /spawnshield block command",
            "Please use the command to edit this option, as it checks for errors when you add them"
    })
    @Getter(AccessLevel.NONE) //Use the cached and thread safe version
    private List<String> blockRegions = Lists.newArrayList("example", "example2");
    @Comments({
            "The prevention mode to put the plugin in",
            "'teleport' teleports the player to their last known location outside of the safezone if they enter a safezone",
            "'knockback' knocks the player back when they enter a safezone and is experimental",
            "'forcefield' is currently in development, and should not be used unless you know what you are doing"
    })
    private BlockMode mode = BlockMode.TELEPORT;
    @Comments({
            "Print out a ****load of information about the plugin",
            "You probably shouldn't use this unless you are testing the plugin"
    })
    private boolean debug = false;

    @Comments(
            "The range in which players will see a forcefield"
    )
    private int forcefieldRange = 50;

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
