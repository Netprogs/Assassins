package com.netprogs.minecraft.plugins.assassins.help;

import java.util.ArrayList;
import java.util.List;

import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
 * <pre>
 * A {@link HelpBook} consists of instances of {@link HelpPage}.
 * {@link HelpPage} consists of instances of {@link HelpSegment}. (parts of a single page).
 * 
 * You then add entries to the HelpSegments using {@link HelpMessage} and {@link HelpText}.
 * </pre>
 * @author Scott
 */
public class HelpBook {

    public static ChatColor COMMAND_COLOR = ChatColor.AQUA;
    public static ChatColor DESCRIPTION_COLOR = ChatColor.YELLOW;

    public static ChatColor SPACER_COLOR = ChatColor.YELLOW;

    public static ChatColor SEGMENT_TITLE_COLOR = ChatColor.LIGHT_PURPLE;

    public static String TITLE_COLOR = "&a";
    public static String PAGE_TITLE_COLOR = "&d";
    public static String PARAMS_COLOR = "&3";

    private static List<HelpPage> helpPages = new ArrayList<HelpPage>();

    public static void addPage(HelpPage page) {

        // add the page
        helpPages.add(page);
    }

    public static boolean sendHelpPage(CommandSender sender, String pluginName, int pageNumber) {

        // Go through the pages and for each segment within it, determine if the user is allowed to use that command
        // Each segment is given the permission so we just use that.
        // If by the time we're done with the page, nothing is left in it, then we won't add it to the final list.
        List<HelpPage> availableHelpPages = new ArrayList<HelpPage>();
        for (HelpPage helpPage : helpPages) {

            HelpPage newHelpPage = new HelpPage(helpPage.getTitle());
            for (HelpSegment helpSegment : helpPage.getSegments()) {

                // if they're allowed access, add the segment
                if (VaultIntegration.getInstance().hasCommandPermission(sender, helpSegment.getCommandType())) {
                    newHelpPage.addSegment(helpSegment);
                }
            }

            // if there are any segments left, add the page to the pages list
            if (newHelpPage.getSegments().size() > 0) {
                availableHelpPages.add(newHelpPage);
            }
        }

        // create and send the help title
        String header = createHeader(pluginName, availableHelpPages, pageNumber);
        sendMessage(sender, header);

        // get the resources
        ResourcesConfig resources = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        // check to make sure the page number is valid
        if (pageNumber <= 0 || pageNumber > availableHelpPages.size()) {

            String helpTitle = resources.getResource("plugin.help.wrongPage");
            sendMessage(sender, ChatColor.RED + helpTitle);
            return false;
        }

        // check to see if the user has any pages available to them
        if (availableHelpPages.size() == 0) {

            String helpTitle = resources.getResource("plugin.help.noneAvailable");
            sendMessage(sender, ChatColor.RED + helpTitle);
            return false;
        }

        HelpPage helpPage = availableHelpPages.get(pageNumber - 1);
        generateHelpMessages(sender, helpPage);

        // send the footer
        MessageUtil.sendFooterMessage(sender, "plugin.help.footer");

        return true;
    }

    private static void generateHelpMessages(CommandSender sender, HelpPage helpPage) {

        // if there was a title, display it now
        if (!StringUtils.isEmpty(helpPage.getTitle())) {
            sendMessage(sender, PAGE_TITLE_COLOR + helpPage.getTitle());
        }

        // now display every segment that was added to this page
        for (HelpSegment helpSegment : helpPage.getSegments()) {

            // if there was a title, display it now
            if (!StringUtils.isEmpty(helpSegment.getTitle())) {
                sendMessage(sender, SEGMENT_TITLE_COLOR + helpSegment.getTitle());
            }

            // display every entry in the segment
            for (IHelpEntry helpEntry : helpSegment.getEntries()) {
                sendMessage(sender, COMMAND_COLOR + "/assassin " + helpEntry.display());
            }
        }
    }

    private static void sendMessage(CommandSender receiver, String message) {

        message = message.replaceAll("(&([A-Fa-f0-9L-Ol-o]))", "\u00A7$2");
        receiver.sendMessage(message);
    }

    private static String createHeader(String pluginName, List<HelpPage> helpPageList, int pageNumber) {

        ResourcesConfig resources = PluginConfig.getInstance().getConfig(ResourcesConfig.class);

        // create our header
        String helpTitle = resources.getResource("plugin.help.header");
        helpTitle = " " + helpTitle + " ";
        helpTitle = helpTitle.replaceAll("<plugin>", pluginName);
        helpTitle += " (" + pageNumber + "/" + helpPageList.size() + ") ";

        String headerSpacer = StringUtils.repeat("-", 52);

        int midPoint = ((headerSpacer.length() / 2) - (helpTitle.length() / 2));
        String start = headerSpacer.substring(0, midPoint);
        String middle = helpTitle;
        String end = headerSpacer.substring(midPoint + helpTitle.length());

        // combine it all into the final header
        String displayHeader = SPACER_COLOR + start + TITLE_COLOR + middle + SPACER_COLOR + end;

        return displayHeader;
    }
}
