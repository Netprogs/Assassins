package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.ArrayList;
import java.util.List;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotOnlineException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
 * Command: /assassins track <player>
 * Points the compass to an estimated location of the player.
 */
public class CommandTrack extends PluginCommand {

    public CommandTrack() {
        super(PluginCommandType.track);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException,
            PlayerNotFoundException, PlayerNotOnlineException {

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

        // convert the sender into a player instance
        Player player = (Player) sender;
        PluginPlayer pluginPlayer = AssassinsPlugin.getStorage().getPlayer(player);

        // verify the player name
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // Get all your contracts for this player. This will return NULL if your hunter timer has expired
        PlayerContracts playerContracts = pluginPlayer.getPlayerContracts(huntedPlayerName);
        if (playerContracts == null) {

            MessageUtil.sendMessage(sender, "assassins.command.track.notHunting", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // get the bukkit player for the hunted player and make sure they're online
        Player huntedPlayer = Bukkit.getPlayer(huntedPlayerName);
        if (huntedPlayer == null) {
            throw new PlayerNotOnlineException(tempPlayerName);
        }

        // get the location of the player using the adjustment value
        int adjustment = AssassinsPlugin.getSettings().getLocationTrackingAdjustment();
        Location huntedPlayerLocation = PlayerUtil.getEstimatedLocation(huntedPlayer.getLocation(), adjustment);

        // check to see if the hunted player is in the same world as the person tracking them
        if (huntedPlayer.getWorld() != player.getWorld()) {

            MessageUtil.sendMessage(sender, "assassins.command.track.notSameWorld", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // now set the compass of the assassin to point in that location
        player.setCompassTarget(huntedPlayerLocation);

        // display to the sender the results also
        MessageParameter locationXParam =
                new MessageParameter("<locationX>", Integer.toString(huntedPlayerLocation.getBlockX()), ChatColor.AQUA);

        MessageParameter locationZParam =
                new MessageParameter("<locationZ>", Integer.toString(huntedPlayerLocation.getBlockZ()), ChatColor.AQUA);

        MessageParameter playerParam = new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA);

        List<MessageParameter> requestParameters = new ArrayList<MessageParameter>();
        requestParameters.add(locationXParam);
        requestParameters.add(locationZParam);
        requestParameters.add(playerParam);

        MessageUtil.sendMessage(sender, "assassins.command.track.compass", ChatColor.GREEN, requestParameters);

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player>");
        mainCommand.setDescription(config.getResource("assassins.command.track.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
