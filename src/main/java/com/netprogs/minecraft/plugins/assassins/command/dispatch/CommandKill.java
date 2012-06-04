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
import com.netprogs.minecraft.plugins.assassins.config.settings.SettingsConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

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
 * Command: /assassins kill <player> <amount> [reason]
 * player - The name of the player to be killed.
 * amount - The amount your paying to have them killed.
 * reason - [optional] If you want to state why you want them killed.
 */
public class CommandKill extends PluginCommand<IPluginSettings> {

    private final Logger logger = Logger.getLogger("Minecraft");

    public CommandKill() {
        super(PluginCommandType.kill);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, PlayerNotFoundException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // check arguments
        if (arguments.size() < 2) {
            throw new ArgumentsMissingException();
        }

        // verify the player
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // get the payment
        double payment = 0;
        try {
            payment = Integer.valueOf(Integer.parseInt(arguments.remove(0)));
        } catch (Exception e) {
            MessageUtil.sendMessage(sender, "assassins.command.kill.invalidPayment", ChatColor.RED);
            return false;
        }

        // check to see if there is a reason
        String reason = "";
        for (int i = 0; i < arguments.size(); i++) {
            reason += arguments.get(i) + " ";
        }

        // check to see if they've reached their limit on number of contracts
        int maxNumContracts = PluginConfig.getInstance().getConfig(SettingsConfig.class).getMaximumContracts();
        PlayerContracts playerContracts = PluginStorage.getInstance().getPlayerContracts(huntedPlayerName);
        if (playerContracts != null && maxNumContracts > 0 && playerContracts.getContracts().size() >= maxNumContracts) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.maxContracts", ChatColor.RED, new MessageParameter(
                    "<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they are under protection
        if (PluginStorage.getInstance().isProtectedPlayer(huntedPlayerName)) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.protectedPlayer", ChatColor.RED,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they're trying to place a contract on themselves
        if (huntedPlayerName.equalsIgnoreCase(sender.getName())) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.cannotContractSelf", ChatColor.RED);
            return false;
        }

        // try to take their money, if they don't have enough, don't continue
        boolean completed = VaultIntegration.getInstance().withdrawContractPayment(sender.getName(), payment);
        if (!completed) {

            MessageUtil.sendMessage(sender, "assassins.command.kill.notEnoughFunds", ChatColor.RED,
                    new MessageParameter("<price>", Double.toString(payment), ChatColor.YELLOW));
            return false;
        }

        // finally, add them to the list
        boolean contractSubmitted =
                PluginStorage.getInstance().createContract(sender.getName(), huntedPlayerName, payment, reason);

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

        ResourcesConfig config = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player> <amount> [reason]");
        mainCommand.setDescription(config.getResource("assassins.command.kill.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
