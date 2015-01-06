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

package me.gnat008.perworldinventory.data;

import me.gnat008.perworldinventory.PerWorldInventory;

public class DataConverter {

    private PerWorldInventory plugin;

    private static DataConverter converter = null;

    private DataConverter(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static DataConverter getInstance(PerWorldInventory plugin) {
        if (converter == null) {
            converter = new DataConverter(plugin);
        }

        return converter;
    }

    public static void disable() {
        converter = null;
    }


}
