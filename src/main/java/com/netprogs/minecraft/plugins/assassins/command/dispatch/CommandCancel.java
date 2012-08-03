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
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;

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
 * Command: /assassins cancel <player>
 * Cancels a current contract for the given player. Can only be run by the person who made the contract.
 */
public class CommandCancel extends PluginCommand {

    public CommandCancel() {
        super(PluginCommandType.cancel);
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
        if (arguments.size() < 1) {
            throw new ArgumentsMissingException();
        }

        // verify the player
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // verify that the sender is actually a player
        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }

        Player player = (Player) sender;

        // try to remove the contract
        Contract contract = AssassinsPlugin.getStorage().removeContract(sender.getName(), huntedPlayerName);
        if (contract != null) {

            Payment payment = contract.getPayment();
            if (payment.getPaymentType() == Payment.Type.cash) {

                // refund the money
                AssassinsPlugin.getVault().depositContractPayment(sender.getName(), payment.getCashAmount());

            } else if (payment.getPaymentType() == Payment.Type.item) {

                // return the item(s) to their inventory
                Material material = Material.getMaterial(payment.getItemId());
                ItemStack itemStack = new ItemStack(material, payment.getItemCount());
                PlayerInventory inventory = player.getInventory();
                inventory.addItem(itemStack);
            }

            MessageUtil.sendMessage(sender, "assassins.command.cancel.completed", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

        } else {

            MessageUtil.sendMessage(sender, "assassins.command.cancel.cannotCancel", ChatColor.RED);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player>");
        mainCommand.setDescription(config.getResource("assassins.command.cancel.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
