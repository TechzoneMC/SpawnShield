/**
 * The MIT License
 * Copyright (c) 2015 Techcable
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
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
import com.google.common.collect.ImmutableList;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collector;

import net.techcable.spawnshield.combattag.CombatTagPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static <T> Collector<T, ?, ImmutableList<T>> immutableListCollector() {
        return Collector.<T, ImmutableList.Builder<T>, ImmutableList<T>>of(
                ImmutableList::builder,
                ImmutableList.Builder::add,
                (builder1, builder2) -> builder1.addAll(builder2.build()),
                ImmutableList.Builder::build
        );
    }

    public static String getAuthors(Plugin plugin) {
        List<String> authors = plugin.getDescription().getAuthors();
        switch (authors.size()) {
            case 0:
                return "unknown author";
            case 1:
                return authors.get(0);
            default:
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < authors.size() - 1; i++) {
                    if (i != 0) builder.append(", ");
                    builder.append(authors.get(i));
                }
                builder.append(", and ");
                builder.append(authors.get(authors.size() - 1));
                return builder.toString();
        }
    }

    public static void assertMainThread() {
        Preconditions.checkState(Bukkit.isPrimaryThread(), "Should only be called on the primary thread");
    }
}
