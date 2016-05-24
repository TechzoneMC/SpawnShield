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
package net.techcable.spawnshield.combattag.legacy;

import lombok.*;

import com.google.common.base.Verify;
import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import net.techcable.spawnshield.combattag.CombatTagPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CombatTagLegacySupport implements CombatTagPlugin {
    public static final String PLUGIN_NAME = "CombatTag";
    private final CombatTagApi api;
    @Getter
    private final Plugin plugin;

    public CombatTagLegacySupport() {
        CombatTagApi api = null;
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        try {
            api = CombatTagApi.getInstance();
        } catch (NoClassDefFoundError | NoSuchMethodError e) { // Old version or not installed
            try {
                if (plugin instanceof CombatTag) {
                    api = new CombatTagApi((CombatTag) plugin);
                }
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {}
        }
        Verify.verify((plugin == null) == (api == null), "Could %s plugin, but could %s api!", plugin == null ? "not find" : "find", api == null ? "not find" : "find");
        this.api = api;
        this.plugin = plugin;
    }

    @Override
    public boolean isTagged(Player player) {
        assertInstalled();
        return api.isInCombat(player);
    }

    @Override
    public long getRemainingTagTime(Player player) {
        assertInstalled();
        return api.getRemainingTagTime(player); // This meats the specifications exactly
    }

    @Override
    public boolean isNPC(Entity entity) {
        assertInstalled();
        return api.isNPC(entity);
    }

    @Override
    public void tag(Player player) {
        assertInstalled();
        api.tagPlayer(player);
    }

    @Override
    public void unTag(Player player) {
        assertInstalled();
        api.untagPlayer(player);
    }

    @Override
    public boolean isInstalled() {
        return api != null;
    }

    private void assertInstalled() {
        if (!isInstalled()) throw new UnsupportedOperationException("Not installed");
    }

}
