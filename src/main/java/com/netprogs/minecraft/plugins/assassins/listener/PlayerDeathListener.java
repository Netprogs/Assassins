package com.netprogs.minecraft.plugins.assassins.listener;

import java.util.ArrayList;
import java.util.List;

import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/*
 * Copyright (C) 2012 Scott Milne
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathEvent(PlayerDeathEvent e) {

        // Player who died
        Player player = (Player) e.getEntity();

        // If the killer was a Player
        if (player.getKiller() instanceof Player) {

            Player attackerPlayer = (Player) player.getKiller();

            // System.out.println("DEATH of player: " + player.getName());
            // System.out.println("DEATH by player: " + attackerPlayer.getName());

            PluginPlayer attackerPluginPlayer = PluginStorage.getInstance().getPlayer(attackerPlayer);

            // Get all your contracts for this player. This will return NULL if your hunter timer has expired
            PlayerContracts playerContracts = attackerPluginPlayer.getPlayerContracts(player.getName());
            if (playerContracts != null) {

                // they have a contract on them, so let's get paid !
                VaultIntegration.getInstance().depositContractPayment(attackerPlayer.getName(),
                        playerContracts.getTotalPayment());

                // remove the contracts
                attackerPluginPlayer.removeContracts(player.getName());

                // show their response message
                MessageParameter paymentParam =
                        new MessageParameter("<payment>", Double.toString(playerContracts.getTotalPayment()),
                                ChatColor.YELLOW);

                MessageParameter playerParam = new MessageParameter("<player>", player.getName(), ChatColor.AQUA);

                MessageParameter assassinParam =
                        new MessageParameter("<assassin>", attackerPlayer.getName(), ChatColor.AQUA);

                List<MessageParameter> requestParameters = new ArrayList<MessageParameter>();
                requestParameters.add(playerParam);
                requestParameters.add(paymentParam);
                requestParameters.add(assassinParam);

                MessageUtil.sendMessage(attackerPlayer, "assassins.contract.assassin.completed", ChatColor.GOLD,
                        requestParameters);

                // try to send a message to the requesting players (if they're online)
                for (Contract contract : playerContracts.getContracts()) {

                    Player requestPlayer = Bukkit.getPlayer(contract.getRequestPlayerName());
                    if (requestPlayer != null) {

                        MessageUtil.sendMessage(requestPlayer, "assassins.contract.requestor.completed",
                                ChatColor.GOLD, requestParameters);
                    }
                }
            }
        }
    }
}
