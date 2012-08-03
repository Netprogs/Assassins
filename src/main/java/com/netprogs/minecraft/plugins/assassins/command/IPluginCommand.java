package com.netprogs.minecraft.plugins.assassins.command;

import java.util.List;

import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotOnlineException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
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

public interface IPluginCommand {

    /**
     * Executes the command.
     * @param plugin
     * @param sender
     * @param arguments
     * @return True if the run was successful. False otherwise.
     * @throws ArgumentsMissingException
     * @throws InvalidPermissionsException
     * @throws SenderNotPlayerException
     * @throws PlayerNotOnlineException
     */
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws ArgumentsMissingException, InvalidPermissionsException, SenderNotPlayerException,
            PlayerNotOnlineException, PlayerNotFoundException;

    /**
     * Used to determine if the sender has permission to execute this command.
     * @param sender The sender (player)
     * @return True if they have access. False otherwise.
     */
    public boolean hasCommandPermission(CommandSender sender);

    /**
     * The command type. This is used to relate commands, permissions, settings and resources.
     * @return The command type.
     */
    public ICommandType getCommandType();

    /**
     * Produces a {@link Help} instance containing the details of the command.
     * @return Help details.
     */
    public HelpSegment help();
}
