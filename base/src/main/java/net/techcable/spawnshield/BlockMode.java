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

import net.techcable.techutils.config.ConfigSerializer;

import org.bukkit.configuration.InvalidConfigurationException;

public enum BlockMode {
    TELEPORT,
    KNOCKBACK,
    FORCEFIELD;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static class BlockModeConverter implements ConfigSerializer<BlockMode> {


        @Override
        public Object serialize(BlockMode blockMode) {
            return blockMode.toString();
        }

        @Override
        public BlockMode deserialize(Object o, Class<? extends BlockMode> aClass) throws InvalidConfigurationException {
            if (!(o instanceof String)) throw new InvalidConfigurationException("block mode must be a string");
            String name = (String) o;
            try {
                return BlockMode.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigurationException(name + " is not a valid mode for SpawnShield");
            }
        }

        @Override
        public boolean canHandle(Class<?> aClass) {
            return BlockMode.class.equals(aClass);
        }
    }
}
