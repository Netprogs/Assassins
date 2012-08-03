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
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.view.ContractWantedItem;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
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
 * Command: /assassins wanted
 * Displays all the available contracts on a per person grouping.
 */
public class CommandWanted extends PluginCommand {

    public CommandWanted() {
        super(PluginCommandType.wanted);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, PlayerNotFoundException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        int pageNumber = 1;
        if (arguments.size() > 0) {
            try {
                pageNumber = Integer.valueOf(Integer.parseInt(arguments.get(0)));
            } catch (Exception e) {
                // don't bother reporting it, just assume page 1
            }
        }

        // get the list of contracts and get the page from them
        List<ContractWantedItem> contracts = AssassinsPlugin.getStorage().getContracts();
        PagedItems<ContractWantedItem> pagedItems = PagedList.getPagedList(contracts, pageNumber, 10);
        if (pagedItems.items == null) {
            MessageUtil.sendHeaderMessage(sender, "assassins.command.wanted.header", pageNumber,
                    pagedItems.numFullPages);
            MessageUtil.sendMessage(sender, "assassins.command.wanted.wrongPage", ChatColor.RED);
            return false;
        }

        // display the header
        MessageUtil.sendHeaderMessage(sender, "assassins.command.wanted.header", pageNumber, pagedItems.numFullPages);

        String countSpacer = StringUtils.repeat("0", 2);

        // grab the sub list for displaying
        for (ContractWantedItem item : pagedItems.items) {

            String numContracts = Integer.toString(item.getNumberOfContracts());
            String numSpacer = countSpacer.substring(numContracts.length());

            if (item.isAvailable()) {

                sender.sendMessage(ChatColor.GREEN + "" + MessageUtil.formatTime(item.getOldestExpiryTimeRemaining())
                        + " " + numSpacer + numContracts + " " + item.getPlayerName());

            } else {

                sender.sendMessage(ChatColor.RED + "" + MessageUtil.formatTime(item.getHunterTimeRemaining()) + " "
                        + numSpacer + numContracts + " " + item.getPlayerName());
            }
        }

        if (pagedItems.items.size() == 0) {

            MessageUtil.sendMessage(sender, "assassins.command.wanted.none", ChatColor.GREEN);

        } else {

            String footerSpacer = StringUtils.repeat("-", 52);
            sender.sendMessage(ChatColor.GOLD + footerSpacer);

            // When the hunters time runs out, goes back to being available.
            MessageUtil.sendMessage(sender, "assassins.command.wanted.notAvailable", ChatColor.RED);
            MessageUtil.sendMessage(sender, "assassins.command.wanted.available", ChatColor.GREEN);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("[1+]");
        mainCommand.setDescription(config.getResource("assassins.command.wanted.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
