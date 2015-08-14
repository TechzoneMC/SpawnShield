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
package net.techcable.spawnshield.nms.versions.v1_7_R4;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Queue;

import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.ChunkCoordIntPair;
import net.minecraft.server.v1_7_R4.ChunkSection;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.World;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.ChunkNotLoadedException;
import net.techcable.spawnshield.nms.NMS;
import net.techcable.techutils.Reflection;

import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class NMSImpl implements NMS {

    private final Field chunkCoordField = Reflection.getOnlyField(PacketPlayOutMultiBlockChange.class, ChunkCoordIntPair.class);
    public static final Field DATA_ARRAY_FIELD = Reflection.getOnlyField(PacketPlayOutMultiBlockChange.class, byte[].class);
    private final Field recordLengthField = Reflection.getOnlyField(PacketPlayOutMultiBlockChange.class, int.class);

    public static int CHANGE_SIZE = Short.BYTES + Short.BYTES;

    @Override
    public void sendMultiBlockChange(Player player, ChunkPos chunkPos, Collection<BlockChange> rawChanges) {
        PacketPlayOutMultiBlockChange packet;
        ImmutableSet<BlockChange> changes = ImmutableSet.copyOf(rawChanges);
        packet = new PacketPlayOutMultiBlockChange();
        ByteArrayDataOutput output = ByteStreams.newDataOutput(CHANGE_SIZE * changes.size());
        for (BlockChange change : changes) {
            int combinedId = (change.getNewMaterial().getId() << 4) | (change.getNewData() & 0xF);
            output.writeShort(change.encodePos());
            output.writeShort(combinedId);
        }
        byte[] changeArray = output.toByteArray();
        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkPos.getX(), chunkPos.getZ());
        Reflection.setField(chunkCoordField, packet, chunkCoord);
        Reflection.setField(DATA_ARRAY_FIELD, packet, changeArray);
        Reflection.setField(recordLengthField, packet, changes.size());
        ProtocolHack.fixPacket(packet);
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        tryQueuePacket(handle.playerConnection, packet);
    }

    private Field processedDisconnectField = Reflection.makeField(PlayerConnection.class, "processedDisconnect");
    private Field channelField = Reflection.getOnlyField(NetworkManager.class, Channel.class); // Obfuscated!
    private Field packetQueueField = Reflection.getOnlyField(NetworkManager.class, Queue.class); // Obfuscated
    private Class<?> queuedPacketClass = Reflection.getNmsClass("QueuedPacket"); // Package private
    private Constructor queuedPacketConstructor = Reflection.makeConstructor(queuedPacketClass, Packet.class, GenericFutureListener[].class);
    private Field queuedPacketBackingField = Reflection.getOnlyField(queuedPacketClass, Packet.class);

    private void tryQueuePacket(PlayerConnection connection, final Packet packet) {
        try {
            if (packet == null || isDisconnected(connection)) {
                return;
            }

            NetworkManager networkManager = connection.networkManager;
            if (isChannelOpen(networkManager)) {
                Channel channel = Reflection.getField(channelField, networkManager);
                channel.write(packet); // Don't flush right now
            } else { // Should be a Queued Packet
                Object queuedPacket = Reflection.callConstructor(queuedPacketConstructor, packet, null);
                Queue<Object> packetQueue = Reflection.getField(packetQueueField, networkManager);
                packetQueue.add(queuedPacket);
            }
        } catch (Throwable t) {
            connection.sendPacket(packet);
        }
    }

    private void flushQueue(NetworkManager networkManager) {
        try {
            if (isChannelOpen(networkManager)) {
                Channel channel = Reflection.getField(channelField, networkManager);
                writeNativeQueue(networkManager, channel);
                channel.flush();
            }
        } catch (Throwable ignored) {
        }
    }

    private void writeNativeQueue(final NetworkManager networkManager, Channel channel) { // Copy functionality from NetworkManager.m
        if (channel == null) {
            channel = Reflection.getField(channelField, networkManager);
        }
        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(new Runnable() {

                @Override
                public void run() {
                    writeNativeQueue(networkManager, null); // Refresh the channel reference "because I can"
                }
            });
            return;
        }
        // By here we are in the event loop
        Queue<Object> nativeQueue = Reflection.getField(packetQueueField, networkManager);
        Object queuedPacket;
        while ((queuedPacket = nativeQueue.poll()) != null) {
            if (!(queuedPacketClass.isInstance(queuedPacket))) continue;
            Packet backingPacket = Reflection.getField(queuedPacketBackingField, queuedPacket);
            if (backingPacket == null) continue;
            channel.write(backingPacket);
        }
    }

    @Override
    public void flushQueue(Player player) {
        if (!(player instanceof CraftPlayer)) return;
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        PlayerConnection connection = handle.playerConnection;
        if (isDisconnected(connection)) return;
        flushQueue(connection.networkManager);
    }

    public boolean isChannelOpen(NetworkManager networkManager) {
        Channel channel = Reflection.getField(channelField, networkManager);
        return channel != null && channel.isOpen();
    }

    public boolean isDisconnected(PlayerConnection connection) {
        return Reflection.getField(processedDisconnectField, connection);
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
