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

import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

import techcable.minecraft.combattag.CombatTagAPI;

/**
 * API to interface with Combat Tag, Combat Tag Reloaded, and PvPManager
 * 
 * @author Techcable
 */
public class CombatAPI {
    private CombatAPI() {}
    /**
     * Returns if a player is combat tagged
     * @param player the player to check
     * @return true if combat tagged
     */
    public static boolean isTagged(Player player) {
        if (hasCombatTag()) {
            return getCombatTagApi().isInCombat(player);
        } else if (hasCombatTagReloaded()) {
            return CombatTagAPI.isTagged(player);
        } else if (hasPvpManager()) {
            return getPlayerHandler().get(player).isInCombat();
        } else {
            return false;
        }
    }
    
    /**
     * Returns the time a player has left in combat
     * <p>
     * Returns -1 if not in combat<br>
     * Returns -2 if not installed
     * 
     * @param player the player to check
     * @return time in milliseconds until the player is no longer in combat
     */
    public static long getRemainingTagTime(Player player) {
        if (hasCombatTag()) {
            return getCombatTagApi().getRemainingTagTime(player);
        } else if (hasCombatTagReloaded()) {
            return CombatTagAPI.getRemainingTagTime(player);
        } else if (hasPvpManager()) {
            long timeLeft = (System.currentTimeMillis() - getPlayerHandler().get(player).getTaggedTime()) - Variables.timeInCombat * 1000; //Very Hacky -- PvPManager doesn't have a public api
            if (timeLeft < 1) return -1; //Not tagged
            return timeLeft;
        } else {
            return -2; //Not installed
        }
    }

    /**
     * Checks if an entity is a NPC
     * @param entity the entity to check
     * @return true if entity is a NPC
     */
    public static boolean isNPC(Entity entity) {
        if (hasCombatTag()) {
            return getCombatTagApi().isNPC(entity);
        } else if (hasCombatTagReloaded()) {
            return CombatTagAPI.isNPC(entity);
        } else {
            return false; //Not installed or PvPManager
        }
    }
    
    /**
     * Tag this player
     * @param player player to tag
     */
    public static void tag(Player player) {
        if (hasCombatTag()) {
            getCombatTagApi().tagPlayer(player);
        } else if (hasCombatTagReloaded()) {
            CombatTagAPI.addTagged(player);
        } else if (hasPvpManager()) {
            getPlayerHandler().tag(getPlayerHandler().get(player));
        }
    }
    
    /**
     * UnTag this player
     * @param player player to un-tag
     */
    public static void unTag(Player player) {
        if (hasCombatTag()) {
            getCombatTagApi().untagPlayer(player);
        } else if (hasCombatTagReloaded()) {
            CombatTagAPI.removeTagged(player);
        } else if (hasPvpManager()) {
             getPlayerHandler().untag(getPlayerHandler().get(player));
        }
    }
    
    /**
     * Return wether a combat-tagging plugin is installed
     * Only CombatTag, CombatTagReloaded, and PvPManager are currently supported
     * @return true if a combat tag plugin is installed
     */
    public static boolean isInstalled() {
        return hasCombatTag() || hasCombatTagReloaded() || hasPvpManager();
    }
    
    //Internal
    private static PlayerHandler getPlayerHandler() {
        PvPManager plugin = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        return plugin.getPlayerHandler();
    }
    
    private static boolean hasCombatTag() {
        return Bukkit.getPluginManager().getPlugin("CombatTag") != null;
    }
    private static boolean hasCombatTagReloaded() {
        return Bukkit.getPluginManager().getPlugin("CombatTagReloaded") != null;
    }
    private static boolean hasPvpManager() {
        return Bukkit.getPluginManager().getPlugin("PvPManager") != null;
    }
    
    private static CombatTagApi combatTagApi;
    private static CombatTagApi getCombatTagApi() {
        if (combatTagApi == null) {
            CombatTag plugin = (CombatTag) Bukkit.getPluginManager().getPlugin("CombatTag");
            CombatAPI.combatTagApi = new CombatTagApi(plugin);
        }
        return combatTagApi;
    }
}