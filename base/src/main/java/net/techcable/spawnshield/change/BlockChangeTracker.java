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
package net.techcable.spawnshield.change;

import com.google.common.collect.*;
import lombok.*;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Tracks block changes and puts them all into a set of multi-block change packets
 */
@RequiredArgsConstructor
@Getter
public class BlockChangeTracker {
    private final Player player;
    private final Multimap<ChunkPos, BlockChange> changes = HashMultimap.create();

    public void addBlockChange(BlockPos pos, Material material, int data) {
        BlockChange change = new BlockChange(pos, material, (byte)data);
        changes.put(pos.getChunkPos(), change);
    }

    public void flush() {
        for (ChunkPos chunk : changes.keySet()) {
            Collection<BlockChange> changeList = changes.get(chunk);
            NMS nms = Utils.getNms();
            nms.sendMultiBlockChange(player, chunk, changeList);
        }
        this.changes.clear();
    }
}
