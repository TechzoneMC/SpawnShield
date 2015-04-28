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
package net.techcable.spawnshield.forcefield;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class ProtectedRegionRegion implements Region {
    @NonNull
    private final ProtectedRegion handle;
    @NonNull
    private final World world;

    @Override
    public boolean contains(BlockPos point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return handle.contains(x, y, z);
    }

    @Override
    public Collection<BlockPos> getPoints() {
        List<BlockVector2D> rawPoints = handle.getPoints();
        Set<BlockPos> points = new HashSet<>();
        int y = handle.getMinimumPoint().getBlockY(); //Works for me :)
        for (BlockVector2D rawPoint : rawPoints) {
            points.add(new BlockPos(rawPoint.getBlockX(), y, rawPoint.getBlockZ(), world));
        }
        return points;
    }

    @Override
    public BlockPos getMin() {
        BlockVector min = handle.getMinimumPoint();
        return new BlockPos(min.getBlockX(), min.getBlockY(), min.getBlockZ(), getWorld());
    }


    @Override
    public BlockPos getMax() {
        BlockVector max = handle.getMaximumPoint();
        return new BlockPos(max.getBlockX(), max.getBlockY(), max.getBlockZ(), getWorld());
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = prime;
        result = prime * result + world.hashCode();
        result = prime * result + handle.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == null) return false;
        if (otherObj == this) return true;
        if (otherObj instanceof ProtectedRegionRegion) {
            ProtectedRegionRegion other = (ProtectedRegionRegion) otherObj;
            if (!getWorld().equals(other.getWorld())) return false;
            if (!getHandle().equals(other.getHandle())) return false;
            return true;
        }
        return false;
    }
}
