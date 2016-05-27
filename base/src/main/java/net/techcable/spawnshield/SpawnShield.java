/**
 * The MIT License
 * Copyright (c) 2015 Techcable
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.techcable.spawnshield;

import lombok.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.techcable.spawnshield.combattag.CombatTagPlugin;
import net.techcable.spawnshield.combattag.legacy.CombatTagLegacySupport;
import net.techcable.spawnshield.combattag.plus.CombatTagPlusSupport;
import net.techcable.spawnshield.combattag.pvpmanager.PvPManagerSupport;
import net.techcable.spawnshield.compat.worldguard6.WorldGuard6Plugin;
import net.techcable.spawnshield.config.SpawnShieldConfig;
import net.techcable.spawnshield.config.SpawnShieldMessages;
import net.techcable.spawnshield.forcefield.ForceFieldListener;
import net.techcable.spawnshield.tasks.KnockbackTask;
import net.techcable.spawnshield.tasks.TeleportSafezoningTask;
import net.techcable.techutils.TechPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class SpawnShield extends TechPlugin<SpawnShieldPlayer> {

    //Configs
    private SpawnShieldConfig settings;
    private SpawnShieldMessages messages;
    //Listeners & Tasks
    private ForceFieldListener forceFieldListener;
    private TeleportSafezoningTask teleportSafezoningTask;
    private KnockbackTask knockbackTask;

    @Override
    protected void startup() {
        getLogger().info("Loading SpawnShield by Techcable");
        if (getCombatTagPlugins().isEmpty()) {
            getLogger().severe("No Combat Tagging Plugin Installed");
            getLogger().severe("Shutting down");
            setEnabled(false);
            return;
        } else {
            getLogger().info("Found " + getCombatTagPlugins().size() + " installed CombatTagPlugins:");
            int i = 1;
            for (CombatTagPlugin plugin : getCombatTagPlugins()) {
                Plugin providingPlugin = JavaPlugin.getProvidingPlugin(plugin.getClass());
                getLogger().info(i + ") " + plugin.getPlugin().getName());
                if (providingPlugin != plugin.getPlugin()) {
                    getLogger().info("\t- this is provided by " + providingPlugin.getName() + " not implemented in " + plugin.getPlugin().getName() + " itself");
                    ;
                    getLogger().info("\t- therefore report bugs with the integration to " + Utils.getAuthors(providingPlugin) + ", not to " + Utils.getAuthors(plugin.getPlugin()));
                }
            }
        }
        settings = new SpawnShieldConfig(this);
        messages = new SpawnShieldMessages(this);
        settings.init();
        messages.init();
        try {
            Metrics metrics = new Metrics(this);
            Metrics.Graph mode = metrics.createGraph("Mode");
            mode.addPlotter(new Metrics.Plotter("Forcefield") {
                @Override
                public int getValue() {
                    return getSettings().getMode() == BlockMode.FORCEFIELD ? 1 : 0;
                }
            });
            mode.addPlotter(new Metrics.Plotter("Knockback") {
                @Override
                public int getValue() {
                    return getSettings().getMode() == BlockMode.KNOCKBACK ? 1 : 0;
                }
            });
            mode.addPlotter(new Metrics.Plotter("Teleport") {
                @Override
                public int getValue() {
                    return getSettings().getMode() == BlockMode.TELEPORT ? 1 : 0;
                }
            });
            Metrics.Graph pluginGraph = metrics.createGraph("Combat Plugin");
            for (CombatTagPlugin plugin : getCombatTagPlugins()) {
                pluginGraph.addPlotter(new Metrics.Plotter(plugin.getPlugin().getName()) {
                    @Override
                    public int getValue() {
                        return 1;
                    }
                });
            }
            metrics.start();
        } catch (IOException e) {
            getLogger().warning("Unable to run metrics");
        }
        if (getSettings().isDebug()) {
            getLogger().setLevel(Level.FINE);
        }
        //Add the protection plugins
        int numPluginsAdded = 0;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            String version = Bukkit.getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (version.startsWith("6")) {
                getLogger().info("Worldguard 6 Detected, Activating support");
                WorldGuard6Plugin hook = new WorldGuard6Plugin();
                getSettings().addProtectionPlugin(hook);
                numPluginsAdded++;
            }
        }
        if (numPluginsAdded == 0) {
            getLogger().severe("No supported protection plugin found, shutting down");
            setEnabled(false);
            return;
        }
        getCommand("spawnshield").setExecutor(new SpawnShieldExecutor(this));
        switch (settings.getMode()) {
            case FORCEFIELD:
                this.forceFieldListener = new ForceFieldListener(this);
                registerListener(forceFieldListener);
                break;
            case TELEPORT:
                teleportSafezoningTask = new TeleportSafezoningTask(this);
                teleportSafezoningTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            case KNOCKBACK:
                knockbackTask = new KnockbackTask(this);
                knockbackTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            default:
                getLogger().severe("[SpawnShield] Unknown Plugin Mode");
                setEnabled(false);
                return;
        }
    }

    public CombatTagPlugin getCombatTagPlugin() {
        return getServer().getServicesManager().getRegistrations(CombatTagPlugin.class).stream()
                .filter((combatService) -> combatService.getProvider().isInstalled()) // Only consider installed plugins :)
                .max((first, second) -> first.getPriority().compareTo(second.getPriority())) // Get the highest priortiy service
                .map(RegisteredServiceProvider::getProvider)
                .orElseThrow(() -> new IllegalStateException("No CombatTagPlugin installed!"));
    }


    public ImmutableList<CombatTagPlugin> getCombatTagPlugins() {
        if (getServer().getServicesManager().getRegistrations(this).isEmpty()) {
            getServer().getServicesManager().register(CombatTagPlugin.class, new CombatTagLegacySupport(), this, ServicePriority.Low);
            getServer().getServicesManager().register(CombatTagPlugin.class, new PvPManagerSupport(), this, ServicePriority.Normal);
            getServer().getServicesManager().register(CombatTagPlugin.class, new CombatTagPlusSupport(), this, ServicePriority.Normal);
        }
        return getServer().getServicesManager().getRegistrations(CombatTagPlugin.class).stream()
                .map(RegisteredServiceProvider::getProvider)
                .filter(CombatTagPlugin::isInstalled) // Only return installed plugins :)
                .collect(Utils.immutableListCollector());
    }

    @Override
    protected void shutdown() {
        if (getSettings() != null) getSettings().save();
    }

    @Override
    public SpawnShieldPlayer createPlayer(UUID id) {
        return new SpawnShieldPlayer(id, this);
    }
}
