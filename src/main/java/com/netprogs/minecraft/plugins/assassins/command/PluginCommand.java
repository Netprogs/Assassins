package com.netprogs.minecraft.plugins.assassins.command;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;

import org.bukkit.Bukkit;
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

public abstract class PluginCommand implements IPluginCommand {

    // The command type is used for command and permissions
    private ICommandType commandType;

    protected PluginCommand(ICommandType commandType) {

        this.commandType = commandType;
    }

    public void verifySenderAsPlayer(CommandSender sender) throws SenderNotPlayerException {

        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }
    }

    public Player getPlayer(String playerName) {
        return Bukkit.getServer().getPlayer(playerName);
    }

    @Override
    public boolean hasCommandPermission(CommandSender sender) {
        return AssassinsPlugin.getVault().hasCommandPermission(sender, commandType);
    }

    @Override
    public ICommandType getCommandType() {
        return commandType;
    }
}
