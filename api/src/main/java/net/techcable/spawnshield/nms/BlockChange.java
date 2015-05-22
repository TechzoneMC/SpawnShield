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
package net.techcable.spawnshield.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ChunkPos;
import org.bukkit.Material;

/**
* Created by Nicholas Schlabach on 5/19/2015.
*/
@Getter
@RequiredArgsConstructor
public class BlockChange {
    private final BlockPos pos;
    private final Material newMaterial;
    private final byte newData;

    public short encodePos() {
        int chunkX = pos.getChunkPos().getX();
        int chunkZ = pos.getChunkPos().getZ();
        return (short) ((pos.getX() - (chunkX << 4)) << 12 | (pos.getZ() - (chunkZ << 4)) << 8 | pos.getY());
    }
}
