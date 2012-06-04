package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.IPluginSettings;
import com.netprogs.minecraft.plugins.assassins.config.settings.SettingsConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.Bukkit;
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
 * Command: /assassins hunt <player>
 * Takes all current active contracts and assigns them to the caller.
 */
public class CommandHunt extends PluginCommand<IPluginSettings> {

    private final Logger logger = Logger.getLogger("Minecraft");

    public CommandHunt() {
        super(PluginCommandType.hunt);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException,
            PlayerNotFoundException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // verify that the sender is actually a player
        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }

        // check arguments
        if (arguments.size() < 1) {
            throw new ArgumentsMissingException();
        }

        // verify the player name
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // convert the sender into a player instance
        Player player = (Player) sender;

        // check to see if they are under protection
        if (PluginStorage.getInstance().isProtectedPlayer(huntedPlayerName)) {

            MessageUtil.sendMessage(sender, "assassins.command.hunt.protectedPlayer", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they're trying to take a contract on themselves
        if (huntedPlayerName.equalsIgnoreCase(sender.getName())) {

            MessageUtil.sendMessage(sender, "assassins.command.hunt.cannotHuntSelf", ChatColor.RED);
            return false;
        }

        // get the list of active contracts for the player being hunted
        PlayerContracts playerContracts = PluginStorage.getInstance().getPlayerContracts(huntedPlayerName);
        if (playerContracts != null && playerContracts.getContracts().size() > 0) {

            // check to see if the player is already being hunted
            if (!playerContracts.isAvailable()) {

                MessageUtil.sendMessage(sender, "assassins.command.hunt.alreadyBeingHunted", ChatColor.RED,
                        new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

                return false;
            }

            int assassinExpireTime = PluginConfig.getInstance().getConfig(SettingsConfig.class).getAssassinExpireTime();
            long currentTime = System.currentTimeMillis();

            // convert to milliseconds
            long assassinTimeLimit = currentTime + (assassinExpireTime * 60 * 1000);

            if (PluginConfig.getInstance().getConfig(SettingsConfig.class).isLoggingDebug()) {

                logger.info("assassinName: " + sender.getName());
                logger.info("assassinExpireTime: " + assassinExpireTime);
                logger.info("currentTime: " + currentTime);
                logger.info("assassinTimeLimit: " + assassinTimeLimit);
            }

            playerContracts.setAssassinPlayerName(sender.getName());
            playerContracts.setAssassinTimeLimit(assassinTimeLimit);

            // save all the changes
            PluginStorage.getInstance().saveAll();

            // add these contracts to their list
            PluginPlayer pluginPlayer = PluginStorage.getInstance().getPlayer(player);
            pluginPlayer.addPlayerContracts(huntedPlayerName, playerContracts);

            // show their response message
            MessageParameter timeLimitParam =
                    new MessageParameter("<timeLimit>", MessageUtil.formatTime(playerContracts
                            .getAssassinTimeRemaining()), ChatColor.RED);

            MessageParameter paymentParam =
                    new MessageParameter("<payment>", Double.toString(playerContracts.getTotalPayment()),
                            ChatColor.YELLOW);

            MessageParameter playerParam = new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA);

            List<MessageParameter> requestParameters = new ArrayList<MessageParameter>();
            requestParameters.add(timeLimitParam);
            requestParameters.add(playerParam);
            requestParameters.add(paymentParam);

            MessageUtil.sendMessage(sender, "assassins.command.hunt.completed", ChatColor.GREEN, requestParameters);

        } else {

            MessageUtil.sendMessage(sender, "assassins.command.hunt.none", ChatColor.GREEN);
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player>");
        mainCommand.setDescription(config.getResource("assassins.command.hunt.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
