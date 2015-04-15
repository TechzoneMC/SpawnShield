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
import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import net.techcable.spawnshield.BlockMode;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.techutils.collect.Pair;
import net.techcable.techutils.yamler.Comments;
import net.techcable.techutils.yamler.Config;
import net.techcable.techutils.yamler.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SpawnShieldMessages extends Config {
    public SpawnShieldMessages(SpawnShield plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "messages.yml");
        CONFIG_HEADER = new String[] {
                "Messagse for spawnshield",
                "Supports color codes with the '&' character"
        };
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (InvalidConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    @Comments({
            "In mode teleport this message will be sent if they enter a safezone in combat",
            "Will be sent at max once a second"
    })
    private String cantEnterSafezone = "&4[SpawnShield] You can't enter a safezone in combat!";

    public String getCantEnterSafezone() {
        return color(this.cantEnterSafezone);
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static SpawnShieldMessages getInstance() {
        return SpawnShield.getInstance().getMessages();
    }
}
