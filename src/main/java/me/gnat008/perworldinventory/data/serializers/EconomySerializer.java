/*
 * Copyright (C) 2014-2016  EbonJaguar
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

package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.JsonObject;
import me.gnat008.perworldinventory.ConsoleLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomySerializer {

    private EconomySerializer() {}

    public static JsonObject serialize(PWIPlayer player, Economy econ) {
        JsonObject data = new JsonObject();

        if (econ.bankBalance(player.getName()).transactionSuccess()) {
            data.addProperty("bank-balance", player.getBankBalance());
        }

        data.addProperty("balance", player.getBalance());

        return data;
    }

    public static void deserialize(Economy econ, JsonObject data, Player player) {
        if (data.has("bank-balance")) {
            ConsoleLogger.debug("[ECON] Depositing " + data.get("bank-balance").getAsDouble() + " to bank of '" + player.getName() + "'!");
            econ.bankDeposit(player.getName(), data.get("bank-balance").getAsDouble());
        }

        if (data.has("balance")) {
            ConsoleLogger.debug("[ECON] Depositing " + data.get("balance").getAsDouble() + " to '" + player.getName() + "'!");
            econ.depositPlayer(player, data.get("balance").getAsDouble());
        }
    }
}
