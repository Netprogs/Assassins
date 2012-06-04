package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PagedList;
import com.netprogs.minecraft.plugins.assassins.command.util.PagedList.PagedItems;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.IPluginSettings;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
 * Command: /assassins contracts [1+]
 * Displays all the hunting contracts of the player.
 */
public class CommandContracts extends PluginCommand<IPluginSettings> {

    private final Logger logger = Logger.getLogger("Minecraft");

    public CommandContracts() {
        super(PluginCommandType.contracts);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // verify that the sender is actually a player
        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }

        // // check arguments
        if (arguments.size() > 1) {
            throw new ArgumentsMissingException();
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

        // convert the sender into a player instance
        Player player = (Player) sender;

        // get the list of contracts for the player
        List<PlayerContracts> contracts = PluginStorage.getInstance().getPlayer(player).getSortedContracts();
        PagedItems<PlayerContracts> pagedItems = PagedList.getPagedList(contracts, pageNumber, 10);
        if (pagedItems.items == null) {

            MessageUtil.sendHeaderMessage(sender, "assassins.command.contracts.header", pageNumber,
                    pagedItems.numFullPages);

            MessageUtil.sendMessage(sender, "assassins.command.contracts.wrongPage", ChatColor.RED);

            return false;
        }

        // display the header
        MessageUtil
                .sendHeaderMessage(sender, "assassins.command.contracts.header", pageNumber, pagedItems.numFullPages);

        // grab the sub list for displaying
        for (PlayerContracts playerContract : pagedItems.items) {

            sender.sendMessage("" + ChatColor.YELLOW + playerContract.getTotalPayment() + " " + ChatColor.RED
                    + MessageUtil.formatTime(playerContract.getAssassinTimeRemaining()) + " " + ChatColor.AQUA
                    + playerContract.getPlayerName());
        }

        if (pagedItems.items.size() == 0) {

            MessageUtil.sendMessage(sender, "assassins.command.contracts.none", ChatColor.GREEN);

        } else {

            // send a footer saying use /assassin view <player> to see the details of the contract
            String footerSpacer = StringUtils.repeat("-", 52);
            sender.sendMessage(ChatColor.GOLD + footerSpacer);
            MessageUtil.sendMessage(sender, "assassins.command.contracts.footer", ChatColor.GREEN);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("[1+]");
        mainCommand.setDescription(config.getResource("assassins.command.contracts.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
