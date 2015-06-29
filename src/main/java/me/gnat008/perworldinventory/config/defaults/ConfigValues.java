/*
 * Copyright (C) 2014-2015  Gnat008
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.config.defaults;

import me.gnat008.perworldinventory.config.ConfigType;
import me.gnat008.perworldinventory.config.ConfigManager;

public enum ConfigValues {

    // First level
    FIRST_START(true),
    MANAGE_GAMEMODES(false),
    SEPARATE_GAMEMODE_INVENTORIES(true),
    USE_MYSQL(false),

    // Second level
    ECONOMY(false),
    ENDER_CHEST(true),
    INVENTORY(true),
    STATS(true),

    // Third level
    CAN_FLY(true),
    DISPLAY_NAME(false),
    EXHAUSTION(true),
    EXP(true),
    FOOD(true),
    FLYING(true),
    GAMEMODE(false),
    HEALTH(true),
    LEVEL(true),
    POTION_EFFECTS(true),
    SATURATION(true),

    // MySQL Config
    HOSTNAME("localhost"),
    PORT(3306),
    DATABASE_NAME("minecraft"),
    PREFIX("pwi_"),
    USERNAME("admin"),
    PASSWORD("password");

    private final Object def;

    ConfigValues(Object def) {
        this.def = def;
    }

    public Object getDef() {
        return def;
    }

    public String getKey() {
        if (this.ordinal() < 4)
            return this.toString().toLowerCase().replace('_', '-');
        else if (this.ordinal() < 8)
            return "player." + this.toString().toLowerCase().replace('_', '-');
        else if (this.ordinal() < 19)
            return "player.stats." + this.toString().toLowerCase().replace('_', '-');
        else {
            return "mysql." + this.toString().toLowerCase().replace('_', '-');
        }
    }

    public void set(Object value) {
        ConfigManager.getInstance().getConfig(ConfigType.CONFIG).getConfig().set(getKey(), value);
    }

    public boolean getBoolean() {
        return ConfigManager.getInstance().getConfig(ConfigType.CONFIG).getConfig().getBoolean(getKey(), (boolean) def);
    }

    public int getInt() {
        return ConfigManager.getInstance().getConfig(ConfigType.CONFIG).getConfig().getInt(getKey(), (int) def);
    }

    public String getString() {
        return ConfigManager.getInstance().getConfig(ConfigType.CONFIG).getConfig().getString(getKey(), (String) def);
    }
}
