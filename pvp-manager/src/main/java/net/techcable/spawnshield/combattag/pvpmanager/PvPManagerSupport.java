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
package net.techcable.spawnshield.combattag.pvpmanager;

import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Utils.CombatUtils;
import net.techcable.spawnshield.combattag.CombatTagPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class PvPManagerSupport implements CombatTagPlugin {
    public static final String PLUGIN_NAME = "PvPManager";
    private final PvPManager plugin;

    public PvPManagerSupport() {
        Plugin rawPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        this.plugin = rawPlugin instanceof PvPManager ? (PvPManager) rawPlugin : null;
    }

    @Override
    public boolean isTagged(Player player) {
        assertInstalled();
        return getPvPPlayer(player).isInCombat();
    }

    @Override
    public long getRemainingTagTime(Player player) {
        assertInstalled();
        if (!isTagged(player)) return -1;
        long timeInCombat = TimeUnit.SECONDS.toMillis(Variables.getTimeInCombat()); // It is seconds in the config
        long expireTime = getPvPPlayer(player).getTaggedTime() + timeInCombat;
        long timeRemaining = System.currentTimeMillis() - expireTime;
        return timeRemaining >= 0 ? timeRemaining : -1;
    }

    @Override
    public boolean isNPC(Entity entity) {
        assertInstalled();
        return false; // PvPManager dont have npcs :)
    }

    @Override
    public void tag(Player player) {
        assertInstalled();
        getPvPPlayer(player).setTagged(true, "plugin");
    }

    @Override
    public void unTag(Player player) {
        assertInstalled();
        getPvPPlayer(player).unTag();
    }

    private PvPlayer getPvPPlayer(Player player) {
        return plugin.getPlayerHandler().get(player);
    }

    @Override
    public boolean isInstalled() {
        return plugin != null;
    }

    private void assertInstalled() {
        if (!isInstalled()) throw new UnsupportedOperationException("Not installed");
    }

}
