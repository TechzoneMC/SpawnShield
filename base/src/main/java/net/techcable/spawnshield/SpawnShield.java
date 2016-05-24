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

import lombok.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;

import net.techcable.spawnshield.combattag.CombatTagPlugin;
import net.techcable.spawnshield.combattag.MultiPluginCombatTagPlugin;
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
import org.reflections.Reflections;

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
        getInstance().getLogger().info("Loading SpawnShield by Techcable");
        if (getCombatTagPlugins().isEmpty()) {
            getInstance().getLogger().severe("No Combat Tagging Plugin Installed");
            getInstance().getLogger().severe("Shutting down");
            setEnabled(false);
            return;
        } else {
            getLogger().info("Found " + getCombatTagPlugins().size() + " installed CombatTagPlugins:");
            int i = 1;
            for (CombatTagPlugin plugin : getCombatTagPlugins()) {
                Plugin providingPlugin = JavaPlugin.getProvidingPlugin(plugin.getClass());
                getLogger().info(i + ") " + plugin.getPlugin().getName());
                if (providingPlugin != plugin.getPlugin()) {
                    getLogger().info("\t- this is provided by " + providingPlugin.getName() + " not implemented in " + plugin.getPlugin().getName() + " itself");;
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
                    return getSettings().getMode() == BlockMode.KNOCKBACK ? 1 : 0;
                }
            });
            metrics.start();
        } catch (IOException e) {
            getInstance().getLogger().warning("Unable to run metrics");
        }
        if (getSettings().isDebug()) {
            getLogger().setLevel(Level.FINE);
        }
        //Add the protection plugins
        int numPluginsAdded = 0;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            String version = Bukkit.getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (version.startsWith("6")) {
                getInstance().getLogger().info("Worldguard 6 Detected, Activating support");
                WorldGuard6Plugin hook = new WorldGuard6Plugin();
                getSettings().addProtectionPlugin(hook);
                numPluginsAdded++;
            }
        }
        if (numPluginsAdded == 0) {
            getInstance().getLogger().severe("No supported protection plugin found, shutting down");
            setEnabled(false);
            return;
        }
        getCommand("spawnshield").setExecutor(new SpawnShieldExecutor());
        switch (settings.getMode()) {
            case FORCEFIELD :
                this.forceFieldListener = new ForceFieldListener(this);
                registerListener(forceFieldListener);
                break;
            case TELEPORT :
                teleportSafezoningTask = new TeleportSafezoningTask(this);
                teleportSafezoningTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            case KNOCKBACK :
                knockbackTask = new KnockbackTask(this);
                knockbackTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            default :
                getInstance().getLogger().severe("[SpawnShield] Unknown Plugin Mode");
                setEnabled(false);
                return;
        }
    }

    public CombatTagPlugin getCombatTagPlugin() {
        ImmutableList<CombatTagPlugin> plugins = getCombatTagPlugins();
        switch (plugins.size()) {
            case 0:
                throw new IllegalStateException("No CombatTagPlugin installed!");
            case 1:
                return plugins.get(0);
            default:
                return new MultiPluginCombatTagPlugin(plugins);
        }
    }


    public ImmutableList<CombatTagPlugin> getCombatTagPlugins() {
        if (getServer().getServicesManager().getRegistrations(this).isEmpty()) {
            // Search classpath for our own impls
            String className = CombatTagPlugin.class.getName();
            String packageName = className.substring(0, className.lastIndexOf('.') - 1);
            Set<Class<? extends CombatTagPlugin>> pluginTypes = new Reflections(packageName).getSubTypesOf(CombatTagPlugin.class);
            for (Class<? extends CombatTagPlugin> pluginType : pluginTypes) {
                if (pluginType == MultiPluginCombatTagPlugin.class) continue;
                CombatTagPlugin plugin;
                try {
                    Constructor<? extends CombatTagPlugin> c = pluginType.getConstructor();
                    c.setAccessible(true);
                    plugin = c.newInstance();
                } catch (NoSuchMethodException | InstantiationException e) {
                    continue;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to call setAccessible() on " + pluginType.getSimpleName());
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getTargetException();
                    throw new RuntimeException("new " + pluginType.getSimpleName() + "() threw an exception", cause);
                }
                getServer().getServicesManager().register(CombatTagPlugin.class, plugin, this, ServicePriority.Normal);
            }
        }
        return getServer().getServicesManager().getRegistrations(CombatTagPlugin.class).stream()
                .map(RegisteredServiceProvider::getProvider)
                .filter(CombatTagPlugin::isInstalled) // Only return installed plugins :)
                .collect(Collector.<CombatTagPlugin, ImmutableList.Builder<CombatTagPlugin>, ImmutableList<CombatTagPlugin>>of(
                        ImmutableList::builder,
                        ImmutableList.Builder::add,
                        (builder1, builder2) -> builder1.addAll(builder2.build()),
                        ImmutableList.Builder::build
                ));
    }

    @Override
    protected void shutdown() {
        if (getSettings() != null) getSettings().save();
    }

    @Override
    public SpawnShieldPlayer createPlayer(UUID id) {
        return new SpawnShieldPlayer(id, this);
    }

    public static SpawnShield getInstance() {
        return JavaPlugin.getPlugin(SpawnShield.class);
    }
}
