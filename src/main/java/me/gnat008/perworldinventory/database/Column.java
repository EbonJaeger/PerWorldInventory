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

package me.gnat008.perworldinventory.database;

import java.util.ArrayList;
import java.util.List;

public class Column {

    private List<String> parts = new ArrayList<>();
    
    public Column(String name) {
        parts.add(name);
    }

    public Column type(String type) {
        parts.add(type);
        return this;
    }
    
    public Column type(String type, int size) {
        parts.add(type + "(" + size + ")");
        return this;
    }
    
    public Column notNull() {
        parts.add("NOT NULL");
        return this;
    }
    
    public Column unique() {
        parts.add("UNIQUE");
        return this;
    }
    
    public Column primaryKey() {
        parts.add("PRIMARY KEY");
        return this;
    }
    
    public Column autoIncrement() {
        parts.add("AUTO_INCREMENT");
        return this;
    }
    
    public String buildColumn() {
        String column = "";
        for (String part : parts) {
            column += part + " ";
        }
        
        return column.substring(0, column.length() - 1);
    }
}
