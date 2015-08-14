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
package net.techcable.spawnshield.nms.versions.v1_8_R3;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange.MultiBlockChangeInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.World;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.ChunkNotLoadedException;
import net.techcable.spawnshield.nms.NMS;
import net.techcable.techutils.Reflection;

import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;

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
            MultiBlockChangeInfo info = packet.new MultiBlockChangeInfo(change.encodePos(), data);
            changeArray[i] = info;
            i++;
        }
        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkPos.getX(), chunkPos.getZ());
        Reflection.setField(chunkCoordField, packet, chunkCoord);
        Reflection.setField(dataArray, packet, changeArray);
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        handle.playerConnection.sendPacket(packet);
    }

    public int getDirect(BlockPos pos) throws ChunkNotLoadedException {
        World world = ((CraftWorld) pos.getWorld()).getHandle();
        ChunkPos chunkPos = pos.getChunkPos();
        Chunk chunk = world.getChunkIfLoaded(chunkPos.getX(), chunkPos.getZ());
        if (chunk == null) throw new ChunkNotLoadedException();
        int sectionId = pos.getSection();
        ChunkSection section = chunk.getSections()[sectionId];
        int sectionPos = ((pos.getY() & 0xF) << 8) | (pos.getRelativeZ() << 4) | pos.getRelativeZ();
        return section.getIdArray()[sectionPos];
    }

    @Override
    public int getDirectId(BlockPos pos) throws ChunkNotLoadedException {
        return getDirect(pos) >> 4;
    }

    @Override
    public int getDirectMeta(BlockPos pos) throws ChunkNotLoadedException {
        return getDirect(pos) & 0xF;
    }
}
