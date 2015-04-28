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
package net.techcable.spawnshield;

import lombok.Getter;
import net.techcable.spawnshield.config.SpawnShieldConfig;
import net.techcable.spawnshield.config.SpawnShieldMessages;
import net.techcable.spawnshield.forcefield.ForceFieldListener;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import net.techcable.spawnshield.tasks.KnockbackTask;
import net.techcable.spawnshield.tasks.TeleportSafezoningTask;
import net.techcable.techutils.TechPlugin;
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
        getCommand("spawnshield").setExecutor(new SpawnShieldExecutor());
        switch (settings.getMode()) {
            case FORCEFIELD :
                Utils.warning("Force field mode is currently unsupported");
                this.forceFieldListener = new ForceFieldListener();
                registerListener(forceFieldListener);
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
