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
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.techcable.techutils.Reflection;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import javax.annotation.Nullable;

import static net.techcable.techutils.Reflection.*;

/**
 * class to interface with Combat Tag, Combat Tag Reloaded, and PvPManager
 * 
 * @author Techcable
 */
public class CombatTagHelper {
    private static CombatTagPlugin delegate;
    static {
        Set<CombatTagPlugin> installed = findInstalled();
        CombatTagPlugin delegate;
        if (installed.size() == 1) {
            delegate = Iterables.getFirst(installed, null);
        } else {
            delegate = new MultiPluginCombatTagPlugin(installed);
        }
        CombatTagHelper.delegate = delegate;
    }

    public static boolean isTagged(Player player) {
        return delegate.isTagged(player);
    }

    public static void unTag(Player player) {
        delegate.unTag(player);
    }

    public static void tag(Player player) {
        delegate.tag(player);
    }

    public static boolean isInstalled() {
        return delegate.isInstalled();
    }

    public static boolean isNPC(Entity entity) {
        return delegate.isNPC(entity);
    }

    public static long getRemainingTagTime(Player player) {
        return delegate.getRemainingTagTime(player);
    }

    public static Set<CombatTagPlugin> findInstalled() {
        Set<CombatTagPlugin> installed = new HashSet<>();
        String className = CombatTagHelper.class.getName();
        String packageName = className.substring(0, className.lastIndexOf('.') - 1);
        Set<Class<? extends CombatTagPlugin>> pluginTypes = new Reflections(packageName).getSubTypesOf(CombatTagPlugin.class);
        for (Class<? extends CombatTagPlugin> pluginType : pluginTypes) {
            if (pluginType == MultiPluginCombatTagPlugin.class) continue;
            CombatTagPlugin plugin;
            try {
                Constructor<? extends CombatTagPlugin> c = pluginType.getConstructor();
                c.setAccessible(true);
                plugin = c.newInstance();
            } catch (NoSuchMethodException | InstantiationException e) {
                continue;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to call setAccessible() on " + pluginType.getSimpleName());
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                throw new RuntimeException("new " + pluginType.getSimpleName() + "() threw an exception", cause);
            }
            if (plugin.isInstalled()) {
                installed.add(plugin);
            }
        }
        return installed;
    }

}
