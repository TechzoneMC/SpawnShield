/**
 * The MIT License
 * Copyright (c) 2015 Techcable
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.techcable.spawnshield.combattag;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface CombatTagPlugin {

    /**
     * Returns if a player is combat tagged
     *
     * @param player the player to check
     * @return true if combat tagged
     * @throws UnsupportedOperationException if no combat tag plugin is installed
     */
    public boolean isTagged(Player player);

    /**
     * Returns the time a player has left in combat
     * <p>
     * Returns -1 if not in combat
     *
     * @param player the player to check
     * @return time in milliseconds until the player is no longer in combat
     * @throws UnsupportedOperationException if no combat tag plugin is installed
     */
    public long getRemainingTagTime(Player player);

    /**
     * Checks if an entity is a NPC
     *
     * @param entity the entity to check
     * @return true if entity is a NPC
     * @throws UnsupportedOperationException if no combat tag plugin is installed
     */
    public boolean isNPC(Entity entity);

    /**
     * Tag this player
     *
     * @param player player to tag
     * @throws UnsupportedOperationException if no combat tag plugin is installed
     */
    public void tag(Player player);

    /**
     * UnTag this player
     *
     * @param player player to un-tag
     * @throws UnsupportedOperationException if no combat tag plugin is installed
     */
    public void unTag(Player player);

    /**
     * Return whether this combat tag plugin is installed
     *
     * @return true if this combat tag plugin is installed
     */
    public boolean isInstalled();

    /**
     * Return the plugin who owns this combat tagging instance
     *
     *
     * @return the plugin
     */
    public Plugin getPlugin();
}
