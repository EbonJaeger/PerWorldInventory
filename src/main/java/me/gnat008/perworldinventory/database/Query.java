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

public class Query {

    private List<String> parts = new ArrayList<>();
    
    public Query createTable(String name, boolean ifNotExists, Column[] columns) {
        String str = "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : " ") + name + "(";
        for (Column column : columns) {
            str += column.buildColumn() + ", ";
        }
        
        str = str.substring(0, str.length() - 2) + ")";
        parts.add(str);
        return this;
    }
    
    /**
     * Add a SELECT statement to the SQL Query.
     * 
     * @param columns An array of columns to select
     * @return This Query object
     */
    public Query select(String... columns) {
        String select = "SELECT ";
        for (String column : columns) {
            select += column + ", ";
        }
        
        parts.add(select.substring(0, select.length() - 2));
        return this;
    }
    
    /**
     * Add an INSERT statement to the SQL Query.
     * 
     * @param table The table to insert into
     * @return This Query object
     */
    public Query insertInto(String table) {
        parts.add("INSERT INTO " + table);
        return this;
    }
    
    /**
     * Add an UPDATE statement to the SQL Query.
     * 
     * @param table The table to update
     * @return This Query Object
     */
    public Query update(String table) {
        parts.add("UPDATE " + table);
        return this;
    }
    
    /**
     * Add a DELETE statement to the SQL Query.
     *
     * @return This Query object
     */
    public Query delete() {
        parts.add("DELETE ");
        return this;
    }

    /**
     * Add a FROM statement to the SQL Query.
     *
     * @param table The table to operate on
     * @return This Query object
     */
    public Query from(String table) {
        parts.add("FROM " + table);
        return this;
    }
    
    /**
     * Add a VALUES statement to the SQL Query.
     * <p>
     * Because we will be using {@link java.sql.PreparedStatement}, 
     * we will only add ?'s as place-holders for the set methods.
     * 
     * @param numOfValues The number of place-holders to add
     * @return This Query object
     */
    public Query values(int numOfValues) {
        String values = "VALUES (";
        for (int i = 0; i < numOfValues; i++) {
            values += "?, ";
        }
        parts.add(values.substring(0, values.length() - 2) + ")");
        return this;
    }
    
    /**
     * Add a SET statement to the SQL Query.
     * <p>
     * Because we will be using {@link java.sql.PreparedStatement}, 
     * we will only add ?'s as place-holders for the set methods.
     * 
     * @param columns A list of column names to add
     * @return This Query object
     */
    public Query set(List<String> columns) {
        String set = "SET ";
        for (int i = 0; i < columns.size(); i++) {
            set += columns.get(i) + " = '?', ";
        }
        parts.add(set.substring(0, set.length() - 2));
        return this;
    }
    
    /**
     * Add a WHERE clause to the SQL Query.
     * <p>
     * Because we will be using {@link java.sql.PreparedStatement}, 
     * we will only add ?'s as place-holders for the set methods.
     * 
     * @param column The column to check
     * @param operator What we are checking for
     * @return This Query object
     */
    public Query where(String column, Operator operator) {
        parts.add("WHERE " + column + operator.get() + "?");
        return this;
    }
    
    /**
     * Add an ADD clause to the SQL Query.
     * <p>
     * Because we will be using {@link java.sql.PreparedStatement}, 
     * we will only add ?'s as place-holders for the set methods.
     * 
     * @param column The column to check
     * @param operator What we are checking for
     * @return This Query object
     */
    public Query and(String column, Operator operator) {
        parts.add("AND " + column + operator.get() + "?");
        return this;
    }
    
    /**
     * Build the query from all of the added parts.
     * 
     * @return The finished SQL query
     */
    public String buildQuery() {
        String query = "";
        for (String part : parts) {
            query += part + " ";
        }
        
        return query.substring(0, query.length() - 1) + ";";
    }
}
