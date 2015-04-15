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
package net.techcable.spawnshield.forcefield; /**
 * (c) 2015 Nicholas Schlabach
 * 
 * You are free to use this class as long as you meet the following conditions
 * 
 * 1. You may not charge money for access to any software conaining or accessing this class
 * 2. Any software containing or accessing this class must be available to the public on either dev.bukkit.org or spigotmc.org
 * 3. ALL source code for any piece of software using this class must be given out
 */
import java.util.Collection;
import java.util.HashSet;

public class BorderFinder {
    private BorderFinder() {}

    public static Collection<BlockPos> getBorderPoints(Region region) {
        HashSet<BlockPos> result = new HashSet<>();
        for (BlockPos point : region.getPoints()) {
            getAlongX(point, region, result);
            getAlongZ(point, region, result);
        }
        return result;
    }
    
    private static void getAlongX(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX() + 1, start.getY(), start.getZ())) { //We are positive
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x++) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        } else { //We are negative or one block
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x--) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        }
    }
    
    private static void getAlongZ(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX(), start.getY(), start.getZ() + 1)) { //We are positive
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z++) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        } else { //We are negative or one block
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z--) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        }
    }
}