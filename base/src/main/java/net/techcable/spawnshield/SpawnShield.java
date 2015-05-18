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

import lombok.Getter;
import net.techcable.spawnshield.compat.worldguard6.WorldGuard6Plugin;
import net.techcable.spawnshield.config.SpawnShieldConfig;
import net.techcable.spawnshield.config.SpawnShieldMessages;
import net.techcable.spawnshield.forcefield.ForceFieldListener;
import net.techcable.spawnshield.forcefield.ForceFieldUpdateRequest;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import net.techcable.spawnshield.tasks.KnockbackTask;
import net.techcable.spawnshield.tasks.TeleportSafezoningTask;
import net.techcable.techutils.TechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;

@Getter
public class SpawnShield extends TechPlugin<SpawnShieldPlayer> {

    //Configs
    private SpawnShieldConfig settings;
    private SpawnShieldMessages messages;
    //Listeners & Tasks
    private ForceFieldListener forceFieldListener;
    private TeleportSafezoningTask teleportSafezoningTask;
    private KnockbackTask knockbackTask;
    private ForceFieldUpdateTask forceFieldUpdateTask;

    @Override
    protected void startup() {
        Utils.info("Loading SpawnShield by Techcable");
        if (!CombatAPI.isInstalled()) {
            Utils.severe("No Combat Tagging Plugin Installed");
            Utils.severe("Shutting down");
            setEnabled(false);
            return;
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
            Utils.warning("Unable to run metrics");
        }
        //Add the protection plugins
        int numPluginsAdded = 0;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            String version = Bukkit.getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (version.startsWith("6")) {
                Utils.info("Worldguard 6 Detected, Activatin support");
                WorldGuard6Plugin hook = new WorldGuard6Plugin();
                getSettings().addProtectionPlugin(hook);
                numPluginsAdded++;
            }
        }
        if (numPluginsAdded == 0) {
            Utils.severe("No supported protection plugin found, shutting down");
            setEnabled(false);
            return;
        }
        getCommand("spawnshield").setExecutor(new SpawnShieldExecutor());
        switch (settings.getMode()) {
            case FORCEFIELD :
                Utils.warning("Force field mode is currently unsupported");
                this.forceFieldListener = new ForceFieldListener();
                registerListener(forceFieldListener);
                this.forceFieldUpdateTask = new ForceFieldUpdateTask();
                forceFieldUpdateTask.runTaskTimerAsynchronously(this, 1, 0); //Every single tick
                break;
            case TELEPORT :
                teleportSafezoningTask = new TeleportSafezoningTask();
                teleportSafezoningTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            case KNOCKBACK :
                knockbackTask = new KnockbackTask();
                knockbackTask.runTaskTimer(this, 5, 5); //Every 1/4 of a tick
                break;
            default :
                Utils.severe("[SpawnShield] Unknown Plugin Mode");
                setEnabled(false);
                return;
        }
    }

    public void request(ForceFieldUpdateRequest request) {
        forceFieldUpdateTask.request(request);
    }
    
    public void clearRequest(UUID id) {
        forceFieldUpdateTask.clearRequest(id);
    }

    @Override
    protected void shutdown() {
        getSettings().save();
    }

    @Override
    public SpawnShieldPlayer createPlayer(UUID id) {
        return new SpawnShieldPlayer(id, this);
    }

    public static SpawnShield getInstance() {
        return JavaPlugin.getPlugin(SpawnShield.class);
    }
}
