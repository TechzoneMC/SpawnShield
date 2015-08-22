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
package net.techcable.spawnshield.combattag;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public final class MultiPluginCombatTagPlugin implements CombatTagPlugin {
    private final Set<CombatTagPlugin> plugins;

    @Override
    public boolean isTagged(Player player) {
        assertPluginsFound();
        for (CombatTagPlugin plugin : plugins) {
            if (plugin.isTagged(player)) return true;
        }
        return false;
    }

    @Override
    public long getRemainingTagTime(Player player) {
        long max = -1;
        for (CombatTagPlugin plugin : plugins) {
            long time = plugin.getRemainingTagTime(player);
            max = Math.max(max, time);
        }
        return max;
    }

    @Override
    public boolean isNPC(Entity entity) {
        assertPluginsFound();
        for (CombatTagPlugin plugin : plugins) {
            if (plugin.isNPC(entity)) return true;
        }
        return false;
    }

    @Override
    public void tag(Player player) {
        assertPluginsFound();
        for (CombatTagPlugin plugin : plugins) {
            plugin.tag(player);
        }
    }

    @Override
    public void unTag(Player player) {
        assertPluginsFound();
        for (CombatTagPlugin plugin : plugins) {
            plugin.unTag(player);
        }
    }

    @Override
    public boolean isInstalled() {
        return plugins.size() > 0;
    }

    private void assertPluginsFound() {
        if (plugins.size() < 1) throw new UnsupportedOperationException("No supported combat tagging plugins found");
    }
}
