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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java8.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.techcable.spawnshield.compat.BlockPos;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.ChunkNotLoadedException;
import net.techcable.spawnshield.nms.NMS;
import net.techcable.techutils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.logging.Level;
import java8.util.stream.Collector;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    private static NMS nms;
    private static final Object nmsInitLock = new Object();

    public static NMS getNms() {
        if (nms == null) {
            synchronized (nmsInitLock) {
                if (nms == null) {
                    String className = "net.techcable.spawnshield.nms.versions." + Reflection.getVersion() + ".NMSImpl";
                    Class<?> nmsClazz = Reflection.getClass(className);
                    Constructor constructor = nmsClazz == null ? null : Reflection.makeConstructor(nmsClazz);
                    if (constructor == null) {
                        warning("This version of minecraft is unsupported");
                        warning("The plugin will still work but forcefields will be less efficient, and players will be spammed with packets");
                        nms = new NMS() {
                            @Override
                            public void sendMultiBlockChange(Player player, ChunkPos chunkPos, Collection<BlockChange> changes) {
                                for (BlockChange change : changes) {
                                    player.sendBlockChange(change.getPos().toLocation(), change.getNewMaterial(), change.getNewData());
                                }
                            }

                            @Override
                            public int getDirectId(BlockPos pos) throws ChunkNotLoadedException {
                                assertLoaded(pos);
                                return pos.getWorld().getBlockTypeIdAt(pos.getX(), pos.getY(), pos.getZ());
                            }

                            @Override
                            public int getDirectMeta(BlockPos pos) throws ChunkNotLoadedException {
                                assertLoaded(pos);
                                return pos.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getData();
                            }

                            private void assertLoaded(BlockPos pos) throws ChunkNotLoadedException {
                                if (!pos.getChunkPos().isLoaded()) throw new ChunkNotLoadedException();
                            }
                        };
                    } else {
                        nms = (NMS) Reflection.callConstructor(constructor);
                        BlockPos.setNmsImplementation(nms);
                    }
                }
            }
        }
        return nms;
    }

    public static void severe(String error) {
        Bukkit.getLogger().severe("[SpawnShield] " + error);
    }

    public static void severe(String error, Throwable t) {
        Bukkit.getLogger().log(Level.SEVERE, "[SpawnShield] " + error, t);
    }

    public static void warning(String error) {
        Bukkit.getLogger().warning("[SpawnShield] " + error);
    }

    public static void debug(String msg) {
        if (isDebug()) {
            info(msg);
        }
    }

    public static boolean isDebug() {
        return SpawnShield.getInstance().getSettings().isDebug();
    }

    public static void info(String msg) {
        Bukkit.getLogger().info("[SpawnShield] " + msg);
    }

    public static void assertMainThread() {
        Preconditions.checkState(Bukkit.isPrimaryThread(), "Should only be called on the primary thread");
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return Collectors.<T, ImmutableSet.Builder<T>, ImmutableSet<T>>of(ImmutableSet::builder, ImmutableSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()), ImmutableSet.Builder::build);
    }

}
