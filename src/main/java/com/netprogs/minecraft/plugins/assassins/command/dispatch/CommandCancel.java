package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.IPluginSettings;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;

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
 * Command: /assassins cancel <player>
 * Cancels a current contract for the given player. Can only be run by the person who made the contract.
 */
public class CommandCancel extends PluginCommand<IPluginSettings> {

    private final Logger logger = Logger.getLogger("Minecraft");

    public CommandCancel() {
        super(PluginCommandType.cancel);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, PlayerNotFoundException {

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

        // try to remove the contract
        Contract contract = PluginStorage.getInstance().removeContract(sender.getName(), huntedPlayerName);
        if (contract != null) {

            // refund the money
            VaultIntegration.getInstance().depositContractPayment(sender.getName(), contract.getPayment());

            MessageUtil.sendMessage(sender, "assassins.command.cancel.completed", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

        } else {

            MessageUtil.sendMessage(sender, "assassins.command.cancel.cannotCancel", ChatColor.RED);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player>");
        mainCommand.setDescription(config.getResource("assassins.command.cancel.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
