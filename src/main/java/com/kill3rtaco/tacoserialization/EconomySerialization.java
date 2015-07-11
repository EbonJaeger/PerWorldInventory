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

package com.kill3rtaco.tacoserialization;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

public class EconomySerialization {

    protected EconomySerialization() {
    }

    public static JSONObject serializeEconomy(Player player, Economy econ) {
        try {
            JSONObject data = new JSONObject();

            if (econ.bankBalance(player.getName()).transactionSuccess()) {
                data.put("bank-balance", econ.bankBalance(player.getName()).balance);
            }

            data.put("balance", econ.getBalance(player));

            return data;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void setEconomy(Economy econ, JSONObject data, Player player) {
        try {
            if (data.has("bank-balance")) {
                econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).balance);
                econ.bankDeposit(player.getName(), data.getDouble("bank-balance"));
            }

            econ.withdrawPlayer(player, econ.getBalance(player));
            econ.depositPlayer(player, data.getDouble("balance"));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
