/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
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

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.techcable.combattag.libs.techutils.TechScheduler;
import net.techcable.spawnshield.forcefield.BlockPos;
import net.techcable.spawnshield.forcefield.ForceFieldUpdateRequest;
import net.techcable.spawnshield.forcefield.Region;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import net.techcable.techutils.entity.TechPlayer;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

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


    private boolean forcefielded = false;
    private Location lastLocationOutsideSafezone = null;
    private long lastCantEnterMessageTime = -1;
    private Collection<BlockPos> lastShownBlocks; //The forcefield blocks last shown to this player
}
