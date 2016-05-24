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
package net.techcable.spawnshield.combattag.heroes;

import lombok.*;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.effects.CombatEffect;

import net.techcable.spawnshield.combattag.CombatTagPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HeroesSupport implements CombatTagPlugin {
    public static final String PLUGIN_NAME = "Heroes";
    @Getter
    private final Heroes plugin;

    public HeroesSupport() {
        Plugin rawPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        this.plugin = rawPlugin instanceof Heroes ? (Heroes) rawPlugin : null;
    }

    @Override
    public boolean isTagged(Player player) {
        assertInstalled();
        return plugin.getCharacterManager().getHero(player).isInCombat();
    }

    @Override
    public long getRemainingTagTime(Player player) {
        assertInstalled();
        long time = plugin.getCharacterManager().getHero(player).getCombatEffect().getTimeLeft();
        if (time <= 0) return -1;
        return time;
    }

    @Override
    public boolean isNPC(Entity entity) {
        assertInstalled();
        return false;
    }

    @Override
    public void tag(Player player) {
        assertInstalled();
        throw new UnsupportedOperationException();
    }

    @Override
    public void unTag(Player player) {
        assertInstalled();
        plugin.getCharacterManager().getHero(player).leaveCombat(CombatEffect.LeaveCombatReason.CUSTOM);
    }

    @Override
    public boolean isInstalled() {
        return plugin != null;
    }

    private void assertInstalled() {
        if (!isInstalled()) throw new UnsupportedOperationException("Not installed");
    }
}
