package com.netprogs.minecraft.plugins.assassins.runnable;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.command.util.PlayerUtil;
import com.netprogs.minecraft.plugins.assassins.config.settings.Blitz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

public class BlitzRunnable implements Runnable {

    private String attackerPlayerName;
    private String huntedPlayerName;

    public BlitzRunnable(String attackerPlayerName, String huntedPlayerName) {

        this.attackerPlayerName = attackerPlayerName;
        this.huntedPlayerName = huntedPlayerName;
    }

    @Override
    public void run() {

        // get the players from bukkit
        Player attackerPlayer = Bukkit.getPlayer(attackerPlayerName);
        Player huntedPlayer = Bukkit.getPlayer(huntedPlayerName);

        if (attackerPlayer != null && huntedPlayer != null) {

            // turn off invisibility
            huntedPlayer.showPlayer(attackerPlayer);

            // set the blitz flag on their player wrapper
            PluginPlayer pluginPlayer = AssassinsPlugin.getStorage().getPlayer(attackerPlayer);
            pluginPlayer.setBlitzActive(false);

            // get the location of the player using the adjustment value
            int adjustment = AssassinsPlugin.getSettings().getLocationTrackingAdjustment();
            Location huntedPlayerLocation = PlayerUtil.getEstimatedLocation(huntedPlayer.getLocation(), adjustment);

            // check to see if the hunted player is in the same world as the person tracking them
            if (huntedPlayer.getLocation().getWorld().getName().equalsIgnoreCase(attackerPlayer.getWorld().getName())) {

                // now set the compass of the assassin to point in that location
                attackerPlayer.setCompassTarget(huntedPlayerLocation);
            }

            // convert the blitz duration into server ticks (20 per second)
            Blitz blitz = AssassinsPlugin.getSettings().getBlitz();
            int durationTicks = blitz.getDuration() * 20;

            // apply the slowness side effect
            if (blitz.isAllowSlow()) {
                PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOW, durationTicks, 1);
                attackerPlayer.addPotionEffect(slowEffect, true);
            }

            // apply the blindness side effect
            if (blitz.isAllowBlindness()) {
                PotionEffect blindnessEffect = new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 1);
                attackerPlayer.addPotionEffect(blindnessEffect, true);
            }

            // apply the confusion side effect
            if (blitz.isAllowConfusion()) {
                PotionEffect confusionEffect = new PotionEffect(PotionEffectType.CONFUSION, durationTicks, 1);
                attackerPlayer.addPotionEffect(confusionEffect, true);
            }

            // tell them their blitz is now off
            MessageUtil.sendMessage(attackerPlayer, "assassins.command.blitz.deactivated", ChatColor.GOLD);
        }
    }
}
