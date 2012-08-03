package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpBook;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;

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

public class CommandHelp extends PluginCommand {

    public CommandHelp() {
        super(PluginCommandType.help);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // if we have an argument, we'll assume it's a page number
        int pageNumber = 1;
        if (arguments.size() > 0) {
            try {
                pageNumber = Integer.valueOf(Integer.parseInt(arguments.get(0)));
            } catch (Exception e) {
                // don't bother reporting it, just assume page 1
            }
        }

        // now ask the book to display a page for us
        HelpBook.sendHelpPage(sender, plugin.getName(), pageNumber);

        // returning false so that we don't so any post-command processing
        return false;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("[1+]");
        mainCommand.setDescription(config.getResource("plugin.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
