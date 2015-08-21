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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.*;
import net.techcable.spawnshield.change.BlockChangeTracker;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.techutils.entity.TechPlayer;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
@Setter
public class SpawnShieldPlayer extends TechPlayer {

    public SpawnShieldPlayer(UUID id, SpawnShield plugin) {
        super(id, plugin);
    }

    @Override
    public SpawnShield getPlugin() {
        return (SpawnShield) super.getPlugin();
    }

    private long lastTagTime = -1;

    @SuppressWarnings("depreciation")
    public boolean isBlocked() {
        if (CombatAPI.isTagged(getEntity())) {
            lastTagTime = CombatAPI.getRemainingTagTime(getEntity()) + System.currentTimeMillis();
            return true;
        } else {
            long delay = getPlugin().getSettings().getAfterCombatDelay();
            if (lastTagTime + delay > System.currentTimeMillis()) return true;
            return false;
        }
    }

    private Location lastLocationOutsideSafezone = null;
    private long lastCantEnterMessageTime = -1;
    private Set<BlockPos> lastShownBlocks; //The forcefield blocks last shown to this player
    /*
    private final ReentrantReadWriteLock lastShownBlocksLock = new ReentrantReadWriteLock();

    public void lockLastShownBlocksRead() {
        lastShownBlocksLock.readLock().lock();
    }

    public void unlockLastShownBlocksRead() {
        lastShownBlocksLock.readLock().unlock();
    }

    public void lockLastShownBlocksWrite() {
        lastShownBlocksLock.writeLock().lock();
    }

    public void unlockLastShownBlocksWrite() {
        lastShownBlocksLock.writeLock().unlock();
    }
    */
}
