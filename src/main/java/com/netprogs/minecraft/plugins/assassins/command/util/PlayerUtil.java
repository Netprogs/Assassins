package com.netprogs.minecraft.plugins.assassins.command.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

public class PlayerUtil {

    public static String getPlayerName(String searchPlayer) {

        // get the base player information
        Player player = Bukkit.getServer().getPlayer(searchPlayer);
        if (player != null) {

            return player.getName();

        } else {

            // check to see if they are off-line
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(searchPlayer);
            if (offlinePlayer != null) {

                // if they've never logged in, then we're not using them
                if (offlinePlayer.getLastPlayed() == 0) {
                    return null;
                }

                // they've logged in before, we're good
                return offlinePlayer.getName();
            }
        }

        return null;
    }

    public static boolean isValidPlayer(String searchPlayer) {

        // get the base player information
        Player player = Bukkit.getServer().getPlayer(searchPlayer);
        if (player != null) {

            return true;

        } else {

            // check to see if they are off-line
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(searchPlayer);
            if (offlinePlayer != null) {

                // if they've never logged in, then we're not using them
                if (offlinePlayer.getLastPlayed() == 0) {
                    return false;
                }

                // they've logged in before, we're good
                return true;
            }
        }

        return false;
    }
}
