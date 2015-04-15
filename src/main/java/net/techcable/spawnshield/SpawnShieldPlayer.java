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
package net.techcable.spawnshield;

import com.google.common.base.Function;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.techcable.combattag.libs.techutils.TechScheduler;
import net.techcable.spawnshield.forcefield.BlockPos;
import net.techcable.spawnshield.forcefield.ForceFieldUpdateRequest;
import net.techcable.spawnshield.forcefield.Region;
import net.techcable.spawnshield.tasks.ForceFieldUpdateTask;
import net.techcable.techutils.entity.TechPlayer;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SpawnShieldPlayer extends TechPlayer {

    public SpawnShieldPlayer(UUID id, SpawnShield plugin) {
        super(id, plugin);
    }

    @Override
    public SpawnShield getPlugin() {
        return (SpawnShield) super.getPlugin();
    }


    private boolean forcefielded = false;
    private Location lastLocationOutsideSafezone = null;
    private long lastCantEnterMessageTime = -1;
    private Collection<BlockPos> lastShownBlocks; //The forcefield blocks last shown to this player
    /**
     * OMG, there can only be one update request at a time, why did you do that Techcable?
     * I did it because PlayerMoveEvents are triggered by packets, which could be fired multiple times a tick
     * If there were 30 entries in the theoretical queue, then the ForceFieldUpdateTask would have to do a lot of work every tick.
     * This ensures that each player only has one forcefieldupdate a tick
     */
    @Setter(AccessLevel.NONE)
    private volatile ForceFieldUpdateRequest updateRequest;

    private final Object forceFieldUpdateLock = new Object();
    @Synchronized("forceFieldUpdateLock")
    public void updateForceField(ForceFieldUpdateRequest request) {
       if (updateRequest == null || updateRequest.isCompleted()) {
           updateRequest = null;
           updateRequest = request;
       }
    }
}
