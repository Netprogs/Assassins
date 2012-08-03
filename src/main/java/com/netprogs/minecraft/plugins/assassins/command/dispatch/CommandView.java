package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PagedList;
import com.netprogs.minecraft.plugins.assassins.command.util.PagedList.PagedItems;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
 * Command: /assassins view <player> [1+]
 * Displays all the available contracts for a specific person.
 */
public class CommandView extends PluginCommand {

    public CommandView() {
        super(PluginCommandType.view);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, PlayerNotFoundException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // // check arguments
        if (arguments.size() < 1) {
            throw new ArgumentsMissingException();
        }

        // the player name should be the first argument
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // there is an optional page number, lets try to get it
        int pageNumber = 1;
        if (arguments.size() > 0) {
            try {
                pageNumber = Integer.valueOf(Integer.parseInt(arguments.get(0)));
            } catch (Exception e) {
                // don't bother reporting it, just assume page 1
            }
        }

        // get the list of contracts for the player
        List<Contract> contracts = AssassinsPlugin.getStorage().getContracts(huntedPlayerName);
        PagedItems<Contract> pagedItems = PagedList.getPagedList(contracts, pageNumber, 5);
        if (pagedItems.items == null) {
            MessageUtil.sendHeaderMessage(sender, "assassins.command.view.header", pageNumber, pagedItems.numFullPages);
            MessageUtil.sendMessage(sender, "assassins.command.view.wrongPage", ChatColor.RED);
            return false;
        }

        // display the header
        MessageUtil.sendHeaderMessage(sender, "assassins.command.view.header", pageNumber, pagedItems.numFullPages);

        // display the contracts
        for (Contract contract : pagedItems.items) {

            String payment = "";

            if (contract.getPayment().getPaymentType() == Payment.Type.cash) {

                payment = Double.toString(contract.getPayment().getCashAmount());

            } else if (contract.getPayment().getPaymentType() == Payment.Type.item) {

                Material material = Material.getMaterial(contract.getPayment().getItemId());
                payment = contract.getPayment().getItemCount() + " " + material.name();
            }

            sender.sendMessage(ChatColor.RED + MessageUtil.formatTime(contract.getTimeRemaining()) + " "
                    + ChatColor.YELLOW + payment + " " + ChatColor.AQUA + contract.getRequestPlayerName());

            if (StringUtils.isNotBlank(contract.getReason())) {
                sender.sendMessage("" + ChatColor.WHITE + contract.getReason() + " ");
            }
        }

        if (pagedItems.items.size() == 0) {

            MessageUtil.sendMessage(sender, "assassins.command.view.none", ChatColor.GREEN);

        } else {

            String footerSpacer = StringUtils.repeat("-", 52);
            sender.sendMessage(ChatColor.GOLD + footerSpacer);

            // When the hunters time runs out, goes back to being available.
            MessageUtil.sendMessage(sender, "assassins.command.view.footer", ChatColor.GREEN);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player> [1+]");
        mainCommand.setDescription(config.getResource("assassins.command.view.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
