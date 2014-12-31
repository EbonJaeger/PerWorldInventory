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

package me.gnat008.perworldinventory;

import me.gnat008.perworldinventory.config.ConfigManager;
import me.gnat008.perworldinventory.data.DataSerializer;
import me.gnat008.perworldinventory.listeners.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.util.Printer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PerWorldInventory extends JavaPlugin {

    private ConfigManager manager;
    private DataSerializer serializer;

    @Override
    public void onEnable() {
        this.manager = ConfigManager.getManager(this);
        this.serializer = DataSerializer.getInstance(this);

        if (!getDataFolder().exists()) {
            new File(getDataFolder() + File.separator + "data").mkdirs();
        }

        manager.reloadConfig();
        manager.reloadWorlds();
        if (manager.getConfig().getBoolean("first-start")) {
            manager.getConfig().set("first-start", false);
            manager.saveConfig();
        }

        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
    }

    @Override
    public void onDisable() {
        ConfigManager.disable();
        Printer.disable();
        serializer.disable();
    }

    public ConfigManager getConfigManager() {
        return this.manager;
    }

    public DataSerializer getSerializer() {
        return this.serializer;
    }
}
