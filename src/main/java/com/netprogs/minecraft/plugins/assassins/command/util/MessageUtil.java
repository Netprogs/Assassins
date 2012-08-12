package com.netprogs.minecraft.plugins.assassins.command.util;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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

public class MessageUtil {

    public static void sendHeaderMessage(CommandSender receiver, String resource, int pageNumber, int maxPages) {

        ResourcesConfig resources = AssassinsPlugin.getResources();

        ChatColor SPACER_COLOR = ChatColor.GOLD;
        ChatColor TITLE_COLOR = ChatColor.GOLD;

        // create our header
        String title = resources.getResource(resource);
        if (title == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        title = " " + title + " ";

        if (pageNumber != 0 && maxPages != 0) {
            title += "(" + pageNumber + "/" + maxPages + ") ";
        }

        String headerSpacer = StringUtils.repeat("-", 52);

        int midPoint = ((headerSpacer.length() / 2) - (title.length() / 2));
        String start = headerSpacer.substring(0, midPoint);
        String middle = title;
        String end = headerSpacer.substring(midPoint + title.length());

        // combine it all into the final header
        String displayHeader = SPACER_COLOR + start + TITLE_COLOR + middle + SPACER_COLOR + end;

        // send the message
        displayHeader = displayHeader.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(displayHeader);
    }

    public static void sendHeaderMessage(CommandSender receiver, String resource) {

        sendHeaderMessage(receiver, resource, 0, 0);
    }

    public static void sendFooterMessage(CommandSender receiver, String resource) {

        ResourcesConfig resources = AssassinsPlugin.getResources();

        ChatColor FOOTER_COLOR = ChatColor.DARK_GRAY;

        // create our header
        String footer = resources.getResource(resource);
        if (footer == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        footer = " " + footer + " ";

        String headerSpacer = StringUtils.repeat(" ", 65);

        int midPoint = ((headerSpacer.length() / 2) - (footer.length() / 2));
        String start = headerSpacer.substring(0, midPoint);
        String middle = footer;
        String end = headerSpacer.substring(midPoint + footer.length());

        // combine it all into the final header
        String displayFooter = FOOTER_COLOR + start + FOOTER_COLOR + middle + end;

        // send the message
        displayFooter = displayFooter.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(displayFooter);
    }

    public static void sendMessage(PluginPlayer receiver, String resource, ChatColor baseColor) {
        Player player = Bukkit.getServer().getPlayer(receiver.getPlayer().getName());
        if (player != null) {
            MessageUtil.sendMessage(player, resource, baseColor);
        }
    }

    public static void sendMessage(CommandSender receiver, String resource) {

        String requestSenderMessage = AssassinsPlugin.getResources().getResource(resource);
        if (requestSenderMessage == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        requestSenderMessage = requestSenderMessage.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(requestSenderMessage);
    }

    public static void sendMessage(CommandSender receiver, String resource, ChatColor baseColor) {

        String requestSenderMessage = AssassinsPlugin.getResources().getResource(resource);
        if (requestSenderMessage == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        requestSenderMessage = requestSenderMessage.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(baseColor + requestSenderMessage);
    }

    public static void sendMessage(CommandSender receiver, String resource, ChatColor baseColor,
            MessageParameter messageVariable) {

        String requestSenderMessage = AssassinsPlugin.getResources().getResource(resource);
        if (requestSenderMessage == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        requestSenderMessage =
                requestSenderMessage.replaceAll(messageVariable.getKey(), messageVariable.getChatColor()
                        + messageVariable.getValue() + baseColor);

        requestSenderMessage = requestSenderMessage.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");

        receiver.sendMessage(baseColor + requestSenderMessage);
    }

    public static void sendMessage(CommandSender receiver, String resource, ChatColor baseColor,
            List<MessageParameter> messageVariables) {

        String requestSenderMessage = AssassinsPlugin.getResources().getResource(resource);
        if (requestSenderMessage == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        for (MessageParameter messageVariable : messageVariables) {

            requestSenderMessage =
                    requestSenderMessage.replaceAll(messageVariable.getKey(), messageVariable.getChatColor()
                            + messageVariable.getValue() + baseColor);
        }

        requestSenderMessage = requestSenderMessage.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(baseColor + requestSenderMessage);
    }

    public static void sendGlobalMessage(String resource, ChatColor baseColor, List<MessageParameter> messageVariables) {

        String requestSenderMessage = AssassinsPlugin.getResources().getResource(resource);
        if (requestSenderMessage == null) {
            AssassinsPlugin.logger().log(Level.SEVERE, "Could not find resource: " + resource);
            return;
        }

        for (MessageParameter messageVariable : messageVariables) {

            requestSenderMessage =
                    requestSenderMessage.replaceAll(messageVariable.getKey(), messageVariable.getChatColor()
                            + messageVariable.getValue() + baseColor);
        }

        requestSenderMessage = requestSenderMessage.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");

        Bukkit.getServer().broadcastMessage(baseColor + requestSenderMessage);
    }

    public static void sendInvalidPermissionsMessage(CommandSender sender) {

        sendMessage(sender, "plugin.error.invalidPermissions", ChatColor.RED);
    }

    public static void sendSenderNotPlayerMessage(CommandSender sender) {

        sendMessage(sender, "plugin.error.senderNotPlayer", ChatColor.RED);
    }

    public static void sendUnknownArgumentsMessage(CommandSender sender) {

        sendMessage(sender, "plugin.error.unknownArguments", ChatColor.RED);
    }

    public static void sendPlayerNotOnlineMessage(CommandSender sender, String offlinePlayerName) {

        sendMessage(sender, "plugin.error.offlinePlayer", ChatColor.RED, new MessageParameter("<player>",
                offlinePlayerName, ChatColor.AQUA));
    }

    public static void sendPlayerNotFoundMessage(CommandSender sender, String playerName) {

        sendMessage(sender, "plugin.error.cannotFindPlayer", ChatColor.RED, new MessageParameter("<player>",
                playerName, ChatColor.AQUA));
    }

    public static String formatTime(long time) {

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat minFormat = new SimpleDateFormat("mm");
        minFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat secFormat = new SimpleDateFormat("ss");
        secFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String hours = hourFormat.format(time);
        String minutes = minFormat.format(time);
        String seconds = secFormat.format(time);

        String timeRemaining = hours + ":" + minutes + ":" + seconds;

        /*
        if (!hours.equals("00")) {
            timeRemaining += hours + "h";
        }

        if (!minutes.equals("00")) {

            if (!hours.equals("00")) {
                timeRemaining += ", ";
            }

            timeRemaining += minutes + "m";
        }

        if (!seconds.equals("00")) {

            if (!minutes.equals("00")) {
                timeRemaining += ", ";
            } else {
                if (!hours.equals("00")) {
                    timeRemaining += ", ";
                }
            }

            timeRemaining += seconds + "s";
        }
        */
        return timeRemaining;
    }

    // public static void sendCommandOnCooldownMessage(CommandSender sender, long timeRemaining) {
    //
    // MessageUtil.sendMessage(sender, "social.error.commandOnCooldown", ChatColor.RED,
    // createCoolDownFormatting(timeRemaining));
    // }
    //
    // public static List<MessageParameter> createCoolDownFormatting(long timeRemaining) {
    //
    // SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    // hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    //
    // SimpleDateFormat minFormat = new SimpleDateFormat("mm");
    // minFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    //
    // SimpleDateFormat secFormat = new SimpleDateFormat("ss");
    // secFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    //
    // MessageParameter hour = new MessageParameter("<hours>", hourFormat.format(timeRemaining), ChatColor.GOLD);
    // MessageParameter min = new MessageParameter("<minutes>", minFormat.format(timeRemaining), ChatColor.GOLD);
    // MessageParameter sec = new MessageParameter("<seconds>", secFormat.format(timeRemaining), ChatColor.GOLD);
    //
    // List<MessageParameter> params = new ArrayList<MessageParameter>();
    // params.add(hour);
    // params.add(min);
    // params.add(sec);
    //
    // return params;
    // }
}
