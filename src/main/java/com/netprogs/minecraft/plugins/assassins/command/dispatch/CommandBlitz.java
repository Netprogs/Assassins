package com.netprogs.minecraft.plugins.assassins.command.dispatch;

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
import com.netprogs.minecraft.plugins.assassins.command.util.TimerUtil;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.Blitz;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.runnable.BlitzRunnable;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
 * Command: /assassins blitz <player>
 * Gives the calling player temporary benefits against the given player for a limited time period.
 */
public class CommandBlitz extends PluginCommand {

    public CommandBlitz() {
        super(PluginCommandType.blitz);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException,
            PlayerNotOnlineException, PlayerNotFoundException {

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

        // check the timer for the command
        long timeRemaining = AssassinsPlugin.getCommandTimer().commandOnTimer(player.getName(), this.getCommandType());
        if (timeRemaining != 0) {

            MessageUtil.sendMessage(sender, "assassins.command.blitz.onTimer", ChatColor.GREEN, new MessageParameter(
                    "<time>", TimerUtil.formatTimeShort(timeRemaining), ChatColor.GREEN));

            return false;
        }

        // get the blitz settings from the configuration
        Blitz blitz = AssassinsPlugin.getSettings().getBlitz();

        // verify the player name
        String tempPlayerName = arguments.remove(0);
        String huntedPlayerName = PlayerUtil.getPlayerName(tempPlayerName);
        if (huntedPlayerName == null) {
            throw new PlayerNotFoundException(tempPlayerName);
        }

        // Get all your contracts for this player. This will return NULL if your hunter timer has expired
        PlayerContracts playerContracts = pluginPlayer.getPlayerContracts(huntedPlayerName);
        if (playerContracts == null) {
            MessageUtil.sendMessage(sender, "assassins.command.blitz.notHunting", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));
            return false;
        }

        // get the bukkit player for the hunted player and make sure they're online
        Player huntedPlayer = Bukkit.getPlayer(huntedPlayerName);
        if (huntedPlayer == null) {
            throw new PlayerNotOnlineException(tempPlayerName);
        }

        // check to see if the hunted player is in the same world as the person tracking them
        if (huntedPlayer.getWorld() != player.getWorld()) {

            MessageUtil.sendMessage(sender, "assassins.command.track.notSameWorld", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        // check to see if they are close enough to their prey to be allowed to blitz them
        if (outOfRange(player.getLocation(), huntedPlayer.getLocation(), blitz.getProximity())) {

            MessageUtil.sendMessage(sender, "assassins.command.blitz.notWithinRange", ChatColor.GREEN,
                    new MessageParameter("<player>", huntedPlayerName, ChatColor.AQUA));

            return false;
        }

        //
        // Apply the blitz effects
        //

        // set the blitz flag on their player wrapper
        pluginPlayer.setBlitzActive(true);

        // set the compass to the new adjustment
        Location huntedPlayerLocation = null;
        int adjustment = blitz.getLocationTrackingAdjustment();
        if (adjustment >= 1) {

            huntedPlayerLocation =
                    PlayerUtil.getEstimatedLocation(huntedPlayer.getLocation(), blitz.getLocationTrackingAdjustment());
            player.setCompassTarget(huntedPlayerLocation);

        } else {

            huntedPlayerLocation = huntedPlayer.getLocation();
            player.setCompassTarget(huntedPlayerLocation);
        }

        // turn them invisible if allowed
        if (blitz.isAllowInvisible()) {
            huntedPlayer.hidePlayer(player);
        }

        // convert the blitz duration into server ticks (20 per second)
        int durationTicks = blitz.getDuration() * 20;

        // give them their jump bonus if allowed
        if (blitz.isAllowJump()) {

            // apply the jump modifier
            PotionEffect jumpEffect = new PotionEffect(PotionEffectType.JUMP, durationTicks, 1);
            player.addPotionEffect(jumpEffect, true);
        }

        // give them their speed bonus if allowed
        if (blitz.isAllowSpeed()) {

            // add their speed bonus
            PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, durationTicks, 1);
            player.addPotionEffect(effect, true);
        }

        // place this command on timer so it can't be used again until it expires
        AssassinsPlugin.getCommandTimer().updateCommandTimer(player.getName(), this.getCommandType(),
                blitz.getCooldown());

        // Create the blitz runnable and place it on a delayed scheduler
        // This will remove the compass adjustment and apply any side effects upon expire of the blitz
        BlitzRunnable runnable = new BlitzRunnable(player.getName(), huntedPlayer.getName());
        AssassinsPlugin.instance.getServer().getScheduler()
                .scheduleSyncDelayedTask(AssassinsPlugin.instance, runnable, durationTicks);

        // tell the player their blitz has been activated
        long timer = System.currentTimeMillis() + (blitz.getDuration() * 1000);
        long remaining = (timer - System.currentTimeMillis());
        MessageUtil.sendMessage(sender, "assassins.command.blitz.activated", ChatColor.GOLD, new MessageParameter(
                "<time>", TimerUtil.formatTimeShort(remaining), ChatColor.GOLD));

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("<player>");
        mainCommand.setDescription(config.getResource("assassins.command.blitz.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }

    private boolean outOfRange(Location senderLocation, Location playerLocation, int proximity) {

        if (senderLocation.equals(playerLocation)) {
            return false;
        }

        if (senderLocation.getWorld() != playerLocation.getWorld()) {
            return true;
        }

        int proximitySquared = (proximity * proximity);
        boolean outOfRange = (senderLocation.distanceSquared(playerLocation) > proximitySquared);
        return outOfRange;
    }
}
