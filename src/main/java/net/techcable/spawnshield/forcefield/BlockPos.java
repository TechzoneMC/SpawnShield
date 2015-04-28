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

import com.google.common.base.Preconditions;
import lombok.experimental.Wither;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import lombok.*;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"x", "y", "z", "world"})
public class BlockPos {

    public BlockPos(Location l) {
        this(l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld());
    }

    @Wither
    private final int x, y, z;
    private final World world;

    public Location toLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    public int distanceSquared(BlockPos other) {
        Preconditions.checkArgument(other.getWorld().equals(getWorld()), "Can't compare the distances of different worlds");
        return square(x - other.x) + square(y - other.y) + square(z - other.z);
    }

    public Material getTypeAt() {
        return Material.getMaterial(getWorld().getBlockTypeIdAt(getX(), getY(), getZ()));
    }

    public byte getDataAt() {
        return getWorld().getBlockAt(getX(), getY(), getZ()).getData();
    }

    @Getter(lazy = true)
    private final ChunkPos chunkPos = new ChunkPos(getX() >> 4, getZ() >> 4, getWorld());

    private static int square(int i) {
        return i * i;
    }
}
