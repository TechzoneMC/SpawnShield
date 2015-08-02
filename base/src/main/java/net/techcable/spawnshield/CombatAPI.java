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

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import net.techcable.techutils.Reflection;

import static net.techcable.techutils.Reflection.*;

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
     *
     * @deprecated please use
     */
    @Deprecated
    public static boolean isTagged(Player player) {
        if (hasCombatTag()) {
            return getCombatTagApi().isInCombat(player);
        } else if (hasPvpManager()) {
            if (isInCombatMethod == null) {
                isInCombatMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.PvPlayer"), "isInCombat");
            }
            return callMethod(isInCombatMethod, getPvpPlayer(player));
        } else if (hasCombatTagPlus()) {
            return getRemainingTagTime(player) < 1;
        } else {
            return false;
        }
    }
    private static Method isInCombatMethod;
    
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
        } else if (hasPvpManager()) {
            if (getTaggedTimeMethod == null) {
                getTaggedTimeMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.PvPlayer"), "getTaggedTime");
            }
            long taggedTime = callMethod(getTaggedTimeMethod, getPvpPlayer(player));
            if (timeInCombatField == null) {
                timeInCombatField = makeField(Reflection.getClass("me.NoChance.PvPManager.Config.Variables"), "timeInCombat");
            }
            long timeInCombat = getField(timeInCombatField, null);
            long timeLeft = (System.currentTimeMillis() - taggedTime) - timeInCombat * 1000; //Very Hacky -- PvPManager doesn't have a public api
            if (timeLeft < 1) return -1; //Not tagged
            return timeLeft;
        } else if (hasCombatTagPlus()){
            Object tag = getTag(player.getUniqueId());
            if (tag == null) return -1;
            if (getTagDurationMethod == null) {
                getTagDurationMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.Tag"), "getTagDuration");
            }
            int timeLeft = callMethod(getTagDurationMethod, tag);
            if (timeLeft < 1) return -1;
            return timeLeft * 1000;
        } else {
            return -2;
        }
    }
    private static Field timeInCombatField;
    private static Method getTaggedTimeMethod;
    private static Method getTagDurationMethod;

    /**
     * Checks if an entity is a NPC
     * @param entity the entity to check
     * @return true if entity is a NPC
     */
    public static boolean isNPC(Entity entity) {
        if (hasCombatTag()) {
            return getCombatTagApi().isNPC(entity);
        } else if (hasCombatTagPlus()) {
            if (isNpcMethod == null) {
                isNpcMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.compat.api.NpcPlayerHelper"), "isNpc", Player.class);
            }
            if (entity instanceof Player) {
                return callMethod(isNpcMethod, getNpcPlayerHelper(), (Player)entity);
            } else {
                return false;
            }
        } else {
            return false; //Not installed or PvPManager
        }
    }
    private static Method isNpcMethod;
    
    /**
     * Tag this player
     * @param player player to tag
     */
    public static void tag(Player player) {
        if (hasCombatTag()) {
            getCombatTagApi().tagPlayer(player);
        } else if (hasPvpManager()) {
            if (playerHandlerTagMethod == null) {
                playerHandlerTagMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.Managers.PlayerHandler"), "tag", Reflection.getClass("me.NoChance.PvPManager.PvPlayer"));
            }
            callMethod(playerHandlerTagMethod, getPlayerHandler(), getPvpPlayer(player));
        } else if (hasCombatTagPlus()) {
            if (tagMethod == null) {
                tagMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.TagManager"), "tag", Player.class, Player.class);
            }
            callMethod(tagMethod, getTagManager(), player, null); //Will probably work
        }
    }
    private static Method tagMethod;
    private static Method playerHandlerTagMethod;
    
    /**
     * UnTag this player
     * @param player player to un-tag
     */
    public static void unTag(Player player) {
        if (hasCombatTag()) {
            getCombatTagApi().untagPlayer(player);
        } else if (hasPvpManager()) {
            if (playerHandlerUntagMethod == null) {
                playerHandlerUntagMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.Managers.PlayerHandler"), "untag", Reflection.getClass("me.NoChance.PvPManager.PvPlayer"));
            }
            callMethod(playerHandlerUntagMethod, getPlayerHandler(), getPvpPlayer(player));
        } else if (hasCombatTagPlus()) {
            if (untagMethod == null) {
                untagMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.TagManager"), "untag", UUID.class);
            }
            callMethod(untagMethod, getTagManager(), player.getUniqueId());
        }
    }
    private static Method untagMethod;
    private static Method playerHandlerUntagMethod;

    /**
     * Return wether a combat-tagging plugin is installed
     * Only CombatTag, CombatTagReloaded, and PvPManager are currently supported
     * @return true if a combat tag plugin is installed
     */
    public static boolean isInstalled() {
        return hasCombatTag() || hasPvpManager() || hasCombatTagPlus();
    }
    
    //Internal
    
    private static Method getNpcPlayerHelperMethod;
    private static Object getNpcPlayerHelper() {
        Object plugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
        if (getNpcPlayerHelperMethod == null) {
            getNpcPlayerHelperMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.CombatTagPlus"), "getNpcPlayerHelper");
        }
        return callMethod(getNpcPlayerHelperMethod, plugin);
    }
    
    private static Method getTagManagerMethod;
    private static Object getTagManager() {
        Object plugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
        if (getTagManagerMethod == null) {
            getTagManagerMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.CombatTagPlus"), "getTagManager");
        }
        return callMethod(getTagManagerMethod, plugin);
    }
    public static Object getTag(UUID playerId) {
        Object tagManager = getTagManager();
        if (getTagMethod == null) {
            getTagMethod = makeMethod(Reflection.getClass("net.minelink.ctplus.TagManager"), "getTag", UUID.class);
        }
        return callMethod(getTagMethod, tagManager, playerId);
    }
    private static Method getTagMethod;

    private static Object getPlayerHandler() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PvPManager");
        if (getPlayerHandlerMethod == null) {
            getPlayerHandlerMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.PvPManager"), "getPlayerHandler");
        }
        return callMethod(getPlayerHandlerMethod, plugin);
    }
    private static Method getPlayerHandlerMethod;

    private static Object getPvpPlayer(Player player) {
        if (playerHandlerGetMethod == null) {
            playerHandlerGetMethod = makeMethod(Reflection.getClass("me.NoChance.PvPManager.Managers.PlayerHandler"), "get", Player.class);
        }
        return callMethod(playerHandlerGetMethod, getPlayerHandler(), player);
    }
    private static Method playerHandlerGetMethod;

    private static boolean hasCombatTag() {
        return Bukkit.getPluginManager().getPlugin("CombatTag") != null;
    }
    private static boolean hasPvpManager() {
        return Bukkit.getPluginManager().getPlugin("PvPManager") != null;
    }
    private static boolean hasCombatTagPlus() {
        return Bukkit.getPluginManager().getPlugin("CombatTagPlus") != null;
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
