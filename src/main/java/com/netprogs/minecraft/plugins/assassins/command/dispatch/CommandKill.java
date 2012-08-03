package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

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

/**
 * Command: /assassins kill <player> <amount> [reason]
 * Command: /assassins kill <player> <count>:<item> [reason]
 * player - The name of the player to be killed.
 * amount - The amount your paying to have them killed.
 * reason - [optional] If you want to state why you want them killed.
 */
public class CommandKill extends PluginCommand {

    public CommandKill() {
        super(PluginCommandType.kill);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, PlayerNotFoundException,
            SenderNotPlayerException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // check arguments
        if (arguments.size() < 2) {
            throw new ArgumentsMissingException();
        }

        // verify that the sender is actually a player
        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }

        Player player = (Player) sender;

        // verify the player being contracted against
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // get the payment, we'll process this later
        String paymentArgument = arguments.remove(0);

        // check to see if there is a reason
        String reason = "";
        for (int i = 0; i < arguments.size(); i++) {
            reason += arguments.get(i) + " ";
        }

        // check to see if they've reached their limit on number of contracts
        int maxNumContracts = AssassinsPlugin.getSettings().getMaximumContracts();
        PlayerContracts playerContracts = AssassinsPlugin.getStorage().getPlayerContracts(huntedPlayerName);
        if (playerContracts != null && maxNumContracts > 0 && playerContracts.getContracts().size() >= maxNumContracts) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.maxContracts", ChatColor.RED, new MessageParameter(
                    "<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they are under protection
        if (AssassinsPlugin.getStorage().isProtectedPlayer(huntedPlayerName)) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.protectedPlayer", ChatColor.RED,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they're trying to place a contract on themselves
        if (huntedPlayerName.equalsIgnoreCase(sender.getName())) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.cannotContractSelf", ChatColor.RED);
            return false;
        }

        // check to see if they already have a contract out on the person
        if (AssassinsPlugin.getStorage().alreadyHasContractOn(sender.getName(), huntedPlayerName)) {
            MessageUtil.sendMessage(sender, "assassins.command.kill.alreadyHasContract", ChatColor.RED);
            return false;
        }

        // process the payment parameter now and handle it accordingly
        Payment payment = new Payment();
        if (paymentArgument.contains(":")) {

            // if it contains a ":" that means they're attempting to use an item as payment
            String[] pieces = paymentArgument.split(":");
            if (pieces.length != 2) {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment", ChatColor.RED);
                return false;
            }

            // get the item count
            int itemCount = 0;
            try {
                itemCount = Integer.valueOf(Integer.parseInt(pieces[0]));
            } catch (Exception e) {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment.count", ChatColor.RED);
                return false;
            }

            // get the material
            Material material = Material.matchMaterial(pieces[1]);
            if (material == null) {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment.material", ChatColor.RED);
                return false;
            }

            // Take the item from their inventory. If they cancel the contract later, we'll return the items then.
            PlayerInventory inventory = player.getInventory();
            if (inventory.contains(material, itemCount)) {

                // create an item stack from the given values and remove it from the inventory
                ItemStack itemStack = new ItemStack(material, itemCount);
                inventory.removeItem(itemStack);

            } else {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment.stack", ChatColor.RED);
                return false;
            }

            // set the type and amount
            payment.setPaymentType(Payment.Type.item);
            payment.setItemCount(itemCount);
            payment.setItemId(material.getId());

        } else if (paymentArgument.equalsIgnoreCase("hand")) {

            // Take the item from their inventory. If they cancel the contract later, we'll return the items then.
            ItemStack handItemStack = player.getItemInHand();
            if (handItemStack.getAmount() == 0) {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment.count", ChatColor.RED);
                return false;
            }

            PlayerInventory inventory = player.getInventory();
            inventory.removeItem(handItemStack);

            // set the type and amount
            payment.setPaymentType(Payment.Type.item);
            payment.setItemCount(handItemStack.getAmount());
            payment.setItemId(handItemStack.getTypeId());

        } else {

            // otherwise process a cash payment
            double cashAmount = 0;
            try {
                cashAmount = Double.valueOf(Double.parseDouble(paymentArgument));
            } catch (Exception e) {
                MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment.cash", ChatColor.RED);
                return false;
            }

            // try to take their money, if they don't have enough, don't continue
            boolean completed = AssassinsPlugin.getVault().withdrawContractPayment(sender.getName(), cashAmount);
            if (!completed) {

                MessageUtil.sendMessage(sender, "assassins.command.kill.notEnoughFunds", ChatColor.RED,
                        new MessageParameter("<price>", Double.toString(cashAmount), ChatColor.YELLOW));
                return false;
            }

            // set the type and amounts
            payment.setPaymentType(Payment.Type.cash);
            payment.setCashAmount(cashAmount);
        }

        // finally, add them to the list
        boolean contractSubmitted =
                AssassinsPlugin.getStorage().createContract(sender.getName(), huntedPlayerName, payment, reason);

        if (contractSubmitted) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.completed", ChatColor.GREEN, new MessageParameter(
                    "<player>", huntedPlayerName, ChatColor.AQUA));

        } else {

            MessageUtil.sendMessage(sender, "assassins.command.kill.alreadyHasContract", ChatColor.RED);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage cashCommand = new HelpMessage();
        cashCommand.setCommand(getCommandType().toString());
        cashCommand.setArguments("<player> <amount> [reason]");
        cashCommand.setDescription(config.getResource("assassins.command.kill.help"));

        HelpMessage itemCommand = new HelpMessage();
        itemCommand.setCommand(getCommandType().toString());
        itemCommand.setArguments("<player> <count:item> [reason]");
        itemCommand.setDescription(config.getResource("assassins.command.kill.help"));

        HelpMessage handCommand = new HelpMessage();
        handCommand.setCommand(getCommandType().toString());
        handCommand.setArguments("<player> hand [reason]");
        handCommand.setDescription(config.getResource("assassins.command.kill.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(cashCommand);
        helpSegment.addEntry(itemCommand);
        helpSegment.addEntry(handCommand);

        return helpSegment;
    }
}
