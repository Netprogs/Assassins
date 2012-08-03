package com.netprogs.minecraft.plugins.assassins.listener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.config.settings.AutoContract;
import com.netprogs.minecraft.plugins.assassins.config.settings.AutoContractor;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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

public class AutoContractListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        // verify that the sender is actually a player
        if (event.getPlayer() instanceof Player) {

            final String logPrefix = "[AutoContract] ";
            Logger logger = AssassinsPlugin.logger();

            Player player = (Player) event.getPlayer();

            // get the auto contract settings
            AutoContractor autoContractor = AssassinsPlugin.getSettings().getAutoContractor();

            // check to see if auto contracts is enabled
            if (!autoContractor.isEnabled()) {
                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info(logPrefix + "is disabled.");
                }
                return;
            }

            // determine if we've reached the maximum number of auto contracts allowed
            if (autoContractor.getMaximumContracts() == AssassinsPlugin.getStorage().getContracts(Contract.Type.auto).size()) {
                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info(logPrefix + "Maximum auto contracts limit reached.");
                }
                return;
            }

            // determine if we're allowed to place an auto contract on this player
            if (AssassinsPlugin.getStorage().getContracts(player.getName()).size() != 0) {
                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info(logPrefix + player.getName() + " already has contracts on them.");
                }
                return;
            }

            // check to see if they are under protection
            if (AssassinsPlugin.getStorage().isProtectedPlayer(player.getName())) {
                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info(logPrefix + player.getName() + " is protected.");
                }
                return;
            }

            // We now need to pick a random number between 1 and 100.
            // This will be used to determine their "chance" of having any of the auto contracts placed on them.
            long currentTime = System.currentTimeMillis();
            Random random = new Random(currentTime);
            int playerChance = random.nextInt(100) + 1;

            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                logger.info(logPrefix + "playerChance: " + playerChance);
            }

            // sort the auto contracts by their chance
            List<AutoContract> autoContracts = autoContractor.getContracts();
            Collections.sort(autoContracts, new Comparator<AutoContract>() {
                public int compare(AutoContract a, AutoContract b) {
                    if (a.getChance() < b.getChance()) {
                        return -1;
                    } else if (a.getChance() == b.getChance()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });

            // go through each auto contract and determine if the player should have the contract placed on them
            for (AutoContract autoContract : autoContracts) {

                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info(logPrefix + "playerChance <= contractChance: " + playerChance + " <= "
                            + autoContract.getChance());
                }

                // if they rolled lower than the chance of the contract, then we use this one
                if (playerChance <= autoContract.getChance()) {

                    String paymentArgument = autoContract.getPayment();

                    // pull out the auto contract details and create an assassin contract from it
                    Payment payment = new Payment();
                    if (paymentArgument.contains(":")) {

                        // if it contains a ":" that means they're attempting to use an item as payment
                        String[] pieces = paymentArgument.split(":");
                        if (pieces.length != 2) {
                            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                                logger.info(logPrefix + "Invalid item payment: " + paymentArgument);
                            }
                            continue;
                        }

                        // get the item count
                        int itemCount = 0;
                        try {
                            itemCount = Integer.valueOf(Integer.parseInt(pieces[0]));
                        } catch (Exception e) {
                            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                                logger.info(logPrefix + "Invalid item count: " + pieces[0]);
                            }
                            continue;
                        }

                        // get the material
                        Material material = Material.matchMaterial(pieces[1]);
                        if (material == null) {
                            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                                logger.info(logPrefix + "Invalid item material: " + pieces[1]);
                            }
                            continue;
                        }

                        // set the type and amount
                        payment.setPaymentType(Payment.Type.item);
                        payment.setItemCount(itemCount);
                        payment.setItemId(material.getId());

                    } else {

                        // otherwise process a cash payment
                        double cashAmount = 0;
                        try {
                            cashAmount = Double.valueOf(Double.parseDouble(paymentArgument));
                        } catch (Exception e) {
                            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                                logger.info(logPrefix + "Invalid cash amount: " + paymentArgument);
                            }
                            continue;
                        }

                        // set the type and amounts
                        payment.setPaymentType(Payment.Type.cash);
                        payment.setCashAmount(cashAmount);
                    }

                    // finally, create the contract
                    AssassinsPlugin.getStorage().createContract(autoContract.getName(), player.getName(), payment,
                            Contract.Type.auto, autoContract.getReason());

                    if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                        logger.info(logPrefix + "Contract created.");
                    }

                    // now we'll return from this method since we only want one contract created
                    return;
                }
            }
        }
    }
}
