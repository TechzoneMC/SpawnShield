/**
 * Copyright (c) 2015 Nicholas Schlabach
 *
 * If there is conflict between two versions of this license, [the version found on gtihub](https://gist.github.com/Techcable/ea146b481870e3736b48) will take precedence.
 *
 * Permission is granted to operate this program provided that:
 * - You have purchased this plugin from Nicholas Schlabach (Techcable)
 * - Techcable has given you his express permission to operate/run this program
 * - Techcable has released this software publicly on either dev.bukkit.org, spigotmc.org or another software hosting site
 * - You do not modify this software in any way
 * - You do not redistrubute this software to anyone else
 * - You do not attempt to deobfuscate, decompile, or reverese engneer this plugin in any way
 * - Nicholas Schlabach (Techcable) reserves the right to change these terms an conditions at any time, with or without warning
 *
 * If any of the above terms are violated this plugin is no longer valid and you must stop using this software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.techcable.spawnshield.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import net.techcable.spawnshield.BlockMode;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.Utils;
import net.techcable.techutils.collect.Pair;
import net.techcable.techutils.yamler.Comments;
import net.techcable.techutils.yamler.Config;
import net.techcable.techutils.yamler.InvalidConfigurationException;
import net.techcable.techutils.yamler.InvalidConverterException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    public void addRegionToBlock(ProtectedRegion r) {
        Utils.assertMainThread();
        blockRegions.add(r.getId());
        refreshRegionsToBlock();
    }

    public void removeRegionToBlock(ProtectedRegion r) {
        Utils.assertMainThread();
        blockRegions.remove(r.getId());
        refreshRegionsToBlock();
    }

    @Comments({
            "The list of worldedit regions to block entry into in combat",
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

    @Synchronized("lock")
    public void refreshRegionsToBlock() {
        this.cachedRegionsToBlock = null;
    }

    @Getter(AccessLevel.NONE)
    private transient Set<Pair<World, ProtectedRegion>> cachedRegionsToBlock;

    /**
     * Why not let the @Synchronised annotation create the lock for me?
     * Because AFAIK, it isn't transient, causing it to be serialized to config
     */
    private final transient Object lock = new Object();
    @Synchronized("lock")
    public Collection<Pair<World, ProtectedRegion>> getRegionsToBlock() {
        if (cachedRegionsToBlock != null) return cachedRegionsToBlock;
        cachedRegionsToBlock = Sets.newConcurrentHashSet();
        Set<String> notFound = Sets.newHashSet(blockRegions);
        for (World world : Bukkit.getWorlds()) {
            RegionManager manager = WGBukkit.getRegionManager(world);
            for (String regionName : blockRegions) {
                if (!manager.hasRegion(regionName)) continue;
                ProtectedRegion region = manager.getRegion(regionName);
                notFound.remove(regionName); //We found it !!
                cachedRegionsToBlock.add(new Pair<World, ProtectedRegion>(world, region));
            }
        }
        //Warn if we couldn't find a worldguard region
        for (String regionName : notFound) {
            Utils.warning(regionName + " is not a known region");
        }
        return cachedRegionsToBlock;
    }
}
