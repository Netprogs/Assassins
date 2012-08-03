package com.netprogs.minecraft.plugins.assassins.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

        Logger logger = AssassinsPlugin.logger();

        // Player who died
        Player player = (Player) e.getEntity();

        // If the killer was a Player
        if (player.getKiller() instanceof Player) {

            Player attackerPlayer = (Player) player.getKiller();

            // System.out.println("DEATH of player: " + player.getName());
            // System.out.println("DEATH by player: " + attackerPlayer.getName());

            PluginPlayer attackerPluginPlayer = AssassinsPlugin.getStorage().getPlayer(attackerPlayer);

            // Get all your contracts for this player. This will return NULL if your hunter timer has expired
            PlayerContracts playerContracts = attackerPluginPlayer.getPlayerContracts(player.getName());
            if (playerContracts != null) {

                // grab the list of contracts for payments
                for (Contract contract : playerContracts.getContracts()) {

                    Payment payment = contract.getPayment();
                    if (payment.getPaymentType() == Payment.Type.cash) {

                        // give the money
                        AssassinsPlugin.getVault().depositContractPayment(attackerPlayer.getName(),
                                payment.getCashAmount());

                        if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                            logger.info(attackerPlayer.getName() + " killed " + player.getName()
                                    + " and received cash: " + payment.getCashAmount());
                        }

                    } else if (payment.getPaymentType() == Payment.Type.item) {

                        // give the item(s) to their inventory
                        Material material = Material.getMaterial(payment.getItemId());
                        ItemStack itemStack = new ItemStack(material, payment.getItemCount());
                        PlayerInventory inventory = attackerPlayer.getInventory();
                        inventory.addItem(itemStack);

                        if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                            logger.info(attackerPlayer.getName() + " killed " + player.getName()
                                    + " and received item: " + itemStack);
                        }
                    }
                }

                // show their response message
                MessageParameter playerParam = new MessageParameter("<player>", player.getName(), ChatColor.AQUA);

                MessageParameter assassinParam =
                        new MessageParameter("<assassin>", attackerPlayer.getName(), ChatColor.AQUA);

                List<MessageParameter> requestParameters = new ArrayList<MessageParameter>();
                requestParameters.add(playerParam);
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

                // tell the killed player they were assassinated
                MessageUtil.sendMessage(player, "assassins.contract.hunted.completed", ChatColor.GOLD);

                // convert the current contracts into revenge one's in case they want to use them later
                boolean revengeContractsStored = playerContracts.storeContractsForRevenge();
                if (!revengeContractsStored) {

                    // remove from the data source if there were no revenge contracts stored
                    AssassinsPlugin.getStorage().removePlayerContracts(player.getName());

                    // tell them they can't use revenge
                    MessageUtil.sendMessage(player, "assassins.contract.hunted.cannotRevenge", ChatColor.GOLD);

                } else {

                    // tell them they can use revenge
                    MessageUtil.sendMessage(player, "assassins.contract.hunted.canRevenge", ChatColor.GOLD);

                    // remove the assassin from the player contracts
                    playerContracts.setAssassinPlayerName(null);
                    playerContracts.setAssassinTimeLimit(0);
                }

                // remove the contracts from the assassin's list
                attackerPluginPlayer.removeContracts(player.getName());

                // save everything
                AssassinsPlugin.getStorage().saveAll();
            }
        }
    }
}
