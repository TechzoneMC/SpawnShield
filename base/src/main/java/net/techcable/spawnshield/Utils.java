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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.techcable.spawnshield.compat.ChunkPos;
import net.techcable.spawnshield.nms.BlockChange;
import net.techcable.spawnshield.nms.NMS;
import net.techcable.techutils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;

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
                        warning("Forcefields will be less efficient, and players will be spammed with packets");
                        nms = new NMS() {
                            @Override
                            public void sendMultiBlockChange(Player player, ChunkPos chunkPos, Collection<BlockChange> changes) {
                                for (BlockChange change : changes) {
                                    player.sendBlockChange(change.getPos().toLocation(), change.getNewMaterial(), change.getNewData());
                                }
                            }
                        };
                    } else {
                        nms = (NMS) Reflection.callConstructor(constructor);
                    }
                }
            }
        }
        return nms;
    }

    public static void severe(String error) {
        Bukkit.getLogger().severe("[SpawnShield] " + error);
    }

    public static void warning(String error) {
        Bukkit.getLogger().warning("[SpawnShield] " + error);
    }

    public static void debug(String msg) {
        if (SpawnShield.getInstance().getSettings().isDebug()) {
            info(msg);
        }
    }

    public static void info(String msg) {
        Bukkit.getLogger().info("[SpawnShield] " + msg);
    }

    public static void assertMainThread() {
        Preconditions.checkState(Bukkit.isPrimaryThread(), "Should only be called on the primary thread");
    }
}
