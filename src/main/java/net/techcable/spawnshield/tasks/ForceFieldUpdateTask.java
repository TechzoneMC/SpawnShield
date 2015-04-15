/**
 * Copyright (c) 2015 Nicholas Schlabach
 *
 * If there is conflict between two versions of this license, [the version found on gtihub](https://gist.github.com/Techcable/ea146b481870e3736b48) will take precedence.
 *
 * Permission is granted to operate this program provided that:
 * - You have purchased this plugin from Nicholas Schlabach (Techcable)
 * - Techcable has given you his express permission to operate/run this program
 * - Techcable has released this software publicly on either dev.bukkit.org, spigotmc.org or another software hosting site
 * - You do not modify this software in any way
 * - You do not redistrubute this software to anyone else
 * - You do not attempt to deobfuscate, decompile, or reverese engneer this plugin in any way
 * - Nicholas Schlabach (Techcable) reserves the right to change these terms an conditions at any time, with or without warning
 *
 * If any of the above terms are violated this plugin is no longer valid and you must stop using this software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.techcable.spawnshield.tasks;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.RequiredArgsConstructor;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.spawnshield.SpawnShieldPlayer;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.forcefield.*;
import net.techcable.techutils.collect.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class ForceFieldUpdateTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player playerEntity : Bukkit.getOnlinePlayers()) {
            SpawnShieldPlayer player = SpawnShield.getInstance().getPlayer(playerEntity);
            ForceFieldUpdateRequest request = player.getUpdateRequest();
            if (request == null || request.isCompleted()) continue;
            processRequest(request);
            request.setCompleted();
        }
    }

    private void processRequest(ForceFieldUpdateRequest request) {
        Set<BlockPos> nearbyBorderPoints = new HashSet<>();
        for (Region region : request.getRegionsToUpdate()) {
            if (!region.getWorld().equals(request.getPosition().getWorld())) continue;
            for (BlockPos borderPoint : getBorders(region)) {
                int distance = borderPoint.distanceSquared(request.getPosition());
                if (distance <= request.getUpdateRadius()) {
                    nearbyBorderPoints.add(borderPoint);
                    Utils.debug("Near Distance " + distance);
                } else {
                    Utils.debug("Far Distance " + distance);
                }
            }
        }
        if (request.getPlayer().getLastShownBlocks() != null) {
            for (BlockPos lastShown : request.getPlayer().getLastShownBlocks()) {
                        /* I'm not sure if minecraft's chunk loading code is thread safe
                         * Plus, a player can't see unloaded chunks, so we don't have to refresh them
                         */
                if (!lastShown.getChunkPos().isLoaded()) continue;
                request.getPlayerEntity().sendBlockChange(lastShown.toLocation(), lastShown.getTypeAt(), lastShown.getDataAt());
            }
        }
        Set<BlockPos> shownBlocks = new HashSet<BlockPos>();
        for (BlockPos borderPoint : nearbyBorderPoints) {
                    /*
                     * I'm not sure if minecraft's chunk loading code is thread safe
                     * Plus no player will be near an unloaded chunk so we don't have to display then
                     */
            if (!borderPoint.getChunkPos().isLoaded()) continue;
            for (int y = 0; y < borderPoint.getWorld().getMaxHeight(); y++) {
                BlockPos pos = borderPoint.withY(y);
                if (pos.getTypeAt().isSolid()) continue; //Don't mess with solid blocks
                request.getPlayerEntity().sendBlockChange(pos.toLocation(), Material.STAINED_GLASS, (byte) 14);
                shownBlocks.add(pos);
            }
        }
        request.getPlayer().setLastShownBlocks(shownBlocks);
    }

    private final Map<Region, Collection<BlockPos>> borderCache = Maps.newHashMap(); //Will only be accessed by a single task, so no need for synchronization
    private Collection<BlockPos> getBorders(Region region) {
        if (borderCache.size() > 50) {
            Utils.severe("Cache exceeded 50 entries, which should never happen.");
            Utils.severe("Clearing cache");
            borderCache.clear();
        }
        if (!borderCache.containsKey(region)) {
            borderCache.put(region, BorderFinder.getBorderPoints(region));
        }
        return borderCache.get(region);
    }
}
