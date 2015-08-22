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
package net.techcable.spawnshield.combattag.plus;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Tag;
import net.techcable.spawnshield.combattag.CombatTagPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class CombatTagPlusSupport implements CombatTagPlugin {
    public static final String PLUGIN_NAME = "CombatTagPlus";
    private final CombatTagPlus plugin;

    public CombatTagPlusSupport() {
        Plugin rawPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        this.plugin = rawPlugin instanceof CombatTagPlus ? (CombatTagPlus) rawPlugin : null;
    }

    @Override
    public boolean isTagged(Player player) {
        assertInstalled();
        return plugin.getTagManager().isTagged(player.getUniqueId());
    }

    @Override
    public long getRemainingTagTime(Player player) {
        assertInstalled();
        if (!isTagged(player)) return -1;
        int seconds = getTag(player).getTagDuration();
        return TimeUnit.SECONDS.toMillis(seconds); // millis
    }

    @Override
    public boolean isNPC(Entity entity) {
        assertInstalled();
        if (entity instanceof Player) {
            return plugin.getNpcPlayerHelper().isNpc((Player)entity);
        } else {
            return false;
        }
    }

    @Override
    public void tag(Player player) {
        assertInstalled();
        plugin.getTagManager().tag(null, player);
    }

    @Override
    public void unTag(Player player) {
        assertInstalled();
        plugin.getTagManager().untag(player.getUniqueId());
    }

    @Override
    public boolean isInstalled() {
        return plugin != null;
    }

    public Tag getTag(Player p) {
        return plugin.getTagManager().getTag(p.getUniqueId());
    }

    private void assertInstalled() {
        if (!isInstalled()) throw new UnsupportedOperationException("Not installed");
    }
}
