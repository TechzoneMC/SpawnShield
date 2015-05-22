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
package net.techcable.spawnshield.nms.versions.v1_8_R1;


import net.minecraft.server.v1_8_R1.*;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.NMS;
import net.techcable.techutils.Reflection;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;

public class NMSImpl implements NMS {

    private final Field chunkCoordField = PacketPlayOutMultiBlockChange.class.getDeclaredFields()[0];
    private final Field dataArray = PacketPlayOutMultiBlockChange.class.getDeclaredFields()[1];
    @Override
    public void sendMultiBlockChange(Player player, ChunkPos chunkPos, Collection<BlockChange> changes) {
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
        MultiBlockChangeInfo[] changeArray = new MultiBlockChangeInfo[changes.size()];
        int i = 0;
        for (BlockChange change : changes) {
            IBlockData data = Block.getById(change.getNewMaterial().getId()).fromLegacyData(change.getNewData());
            MultiBlockChangeInfo info = new MultiBlockChangeInfo(packet, change.encodePos(), data);
            changeArray[i] = info;
            i++;
        }
        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkPos.getX(), chunkPos.getZ());
        Reflection.setField(chunkCoordField, packet, chunkCoord);
        Reflection.setField(dataArray, packet, changeArray);
        EntityPlayer handle = ((CraftPlayer)player).getHandle();
        handle.playerConnection.sendPacket(packet);
    }
}
