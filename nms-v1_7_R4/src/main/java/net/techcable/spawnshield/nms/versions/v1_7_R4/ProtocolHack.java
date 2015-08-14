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

import lombok.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.PacketPlayOutMultiBlockChange;
import net.minecraft.util.gnu.trove.list.TIntList;
import net.minecraft.util.gnu.trove.list.TShortList;
import net.minecraft.util.gnu.trove.list.array.TIntArrayList;
import net.minecraft.util.gnu.trove.list.array.TShortArrayList;
import net.techcable.techutils.Reflection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtocolHack {

    @Getter
    private static final boolean protocolHack = Reflection.getClass("org.spigotmc.ProtocolData") != null;

    private static final Field positionsField = isProtocolHack() ? Reflection.getOnlyField(PacketPlayOutMultiBlockChange.class, short[].class) : null;
    private static final Field chunkField = isProtocolHack() ? Reflection.getOnlyField(PacketPlayOutMultiBlockChange.class, Chunk.class) : null;
    private static final Field blocksField = isProtocolHack() ? Reflection.makeField(PacketPlayOutMultiBlockChange.class, "blocks") : null;

    public static void fixPacket(PacketPlayOutMultiBlockChange packet) {
        if (!isProtocolHack()) return;
        byte[] rawData = Reflection.getField(NMSImpl.DATA_ARRAY_FIELD, packet);
        ByteArrayDataInput input = ByteStreams.newDataInput(rawData);
        TIntList blocks = new TIntArrayList(rawData.length / NMSImpl.CHANGE_SIZE);
        TShortList positions = new TShortArrayList(rawData.length / NMSImpl.CHANGE_SIZE);
        while (true) {
            short pos;
            int combinedId;
            try {
                pos = input.readShort();
                combinedId = input.readShort();
            } catch (IllegalStateException e) {
                break; // Not enough data
            }
            int blockId = (combinedId >> 4) & 0xFFF;
            int data = combinedId & 0xF;
            data = getCorrectedData(blockId, data);
            combinedId = ((blockId & 0xFFF) << 4) | (data & 0xF);
            blocks.add(combinedId);
            positions.add(pos);
        }
        Reflection.setField(blocksField, packet, blocks.toArray());
        Reflection.setField(positionsField, packet, positions.toArray());
        Reflection.setField(chunkField, packet, null); // Unused
    }

    private static final Class<?> spigotDebreakifierClass = Reflection.getClass("org.spigotmc.SpigotDebreakifier");
    private static final Method getCorrectedDataMethod = isProtocolHack() ? Reflection.makeMethod(spigotDebreakifierClass, "getCorrectedData") : null;

    private static int getCorrectedData(int blockId, int data) {
        if (!isProtocolHack()) return data;
        return Reflection.callMethod(getCorrectedDataMethod, blockId, data);
    }
}
