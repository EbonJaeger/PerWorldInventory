/**
 * PerWorldInventory is a multi-world inventory plugin.
 * Copyright (C) 2014 - 2015 Gnat008
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.Logger;

import java.util.ArrayList;

public class PWILogger {

    private Level LogLevel = Level.DEBUG;
    private ArrayList<Handler> handlers = new ArrayList<Handler>();

    public enum Level {
        NONE(0),
        SEVERE(1),
        WARNING(2),
        INFO(3),
        DEBUG(4);

        private int value;

        Level(int value) {
            this.value = value;
        }

    }

    public PWILogger() {
        handlers.add(new ConsoleHandler());
    }

    public Level getLogLevel() {
        return LogLevel;
    }

    public void setLogLevel(Level logLevel) {
        LogLevel = logLevel;
    }

    public void addHandler(Handler handler) {
        if(find(handler.getClass()) == null) {
            handlers.add(handler);
        }
    }

    public void removeHandler(Handler handler) {
        Handler h = find(handler.getClass());
        if(h != null) {
            handlers.remove(h);
        }
    }

    public void info(String message) {
        if(LogLevel.value >= Level.INFO.value) {
            for(Handler handler : handlers) {
                handler.info(message);
            }
        }
    }

    public void warning(String message) {
        if(LogLevel.value >= Level.WARNING.value) {
            for(Handler handler : handlers) {
                handler.warning(message);
            }
        }
    }

    public void severe(String message) {
        if(LogLevel.value >= Level.SEVERE.value) {
            for(Handler handler : handlers) {
                handler.severe(message);
            }
        }
    }

    public void debug(String message) {
        if(LogLevel.value >= Level.DEBUG.value) {
            for(Handler handler : handlers) {
                handler.debug(message);
            }
        }
    }

    private <T> T find(Class<T> clazz) {
        for(Handler o : handlers) {
            if(o != null && o.getClass() == clazz)
            {
                return clazz.cast(o);
            }
        }

        return null;
    }

}
