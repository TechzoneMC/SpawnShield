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
package net.techcable.spawnshield.config;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.techcable.spawnshield.BlockMode;
import net.techcable.spawnshield.RegionManager;
import net.techcable.spawnshield.Utils;
import net.techcable.spawnshield.compat.ProtectionPlugin;
import net.techcable.spawnshield.compat.Region;
import net.techcable.techutils.config.AnnotationConfig;
import net.techcable.techutils.config.Setting;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.google.common.collect.Sets;
import org.bukkit.configuration.InvalidConfigurationException;

@Getter
@RequiredArgsConstructor
public class SpawnShieldConfig extends AnnotationConfig {
    private final RegionManager regionManager;

    @Setting("blockRegions")
    @Getter
    private List<String> blockRegions;

    @Setting("mode")
    private BlockMode mode;

    @Setting("debug")
    private boolean debug;

    @Setting("force-field.range")
    private int forceFieldRange;

    @Setting("afterCombatDelay")
    @Getter(AccessLevel.NONE)
    private int afterCombatDelay;

    @Override
    public void load(File configFile, URL defaultConfigUrl) throws IOException, InvalidConfigurationException {
        super.load(configFile, defaultConfigUrl);
        regionManager.refresh(this);
    }

    @Override
    public void save(File configFile, URL defaultConfigUrl) throws IOException, InvalidConfigurationException {
        regionManager.saveConfig(this);
        super.save(configFile, defaultConfigUrl);
    }

    /**
     * Get the delay in milliseconds players must wait after being tagged until they can re-enter spawn
     *
     * @return the delay in millesconds
     */
    public long getAfterCombatDelay() {
        return TimeUnit.SECONDS.toMillis(afterCombatDelay);
    }
}
