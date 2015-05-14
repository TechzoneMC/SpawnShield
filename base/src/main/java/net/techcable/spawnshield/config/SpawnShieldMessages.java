/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
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

import com.google.common.base.Throwables;
import net.techcable.spawnshield.SpawnShield;
import net.techcable.techutils.collect.Pair;
import net.techcable.techutils.yamler.Comments;
import net.techcable.techutils.yamler.Config;
import net.techcable.techutils.yamler.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SpawnShieldMessages extends Config {
    public SpawnShieldMessages(SpawnShield plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "messages.yml");
        CONFIG_HEADER = new String[] {
                "Messagse for spawnshield",
                "Supports color codes with the '&' character"
        };
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (InvalidConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    @Comments({
            "In mode teleport this message will be sent if they enter a safezone in combat",
            "Will be sent at max once a second"
    })
    private String cantEnterSafezone = "&4[SpawnShield] You can't enter a safezone in combat!";

    public String getCantEnterSafezone() {
        return color(this.cantEnterSafezone);
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static SpawnShieldMessages getInstance() {
        return SpawnShield.getInstance().getMessages();
    }
}
