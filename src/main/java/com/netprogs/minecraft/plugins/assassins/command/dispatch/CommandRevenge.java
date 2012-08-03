package com.netprogs.minecraft.plugins.assassins.command.dispatch;

import java.util.List;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommand;
import com.netprogs.minecraft.plugins.assassins.command.PluginCommandType;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageParameter;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.help.HelpMessage;
import com.netprogs.minecraft.plugins.assassins.help.HelpSegment;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract.Type;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

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
 * Command: /assassin revenge
 * Get revenge on the people who requested you be killed !
 */
public class CommandRevenge extends PluginCommand {

    public CommandRevenge() {
        super(PluginCommandType.revenge);
    }

    @Override
    public boolean run(JavaPlugin plugin, CommandSender sender, List<String> arguments)
            throws InvalidPermissionsException, SenderNotPlayerException {

        // check permissions
        if (!hasCommandPermission(sender)) {
            throw new InvalidPermissionsException();
        }

        // verify that the sender is actually a player
        if (!(sender instanceof Player)) {
            throw new SenderNotPlayerException();
        }

        // get the players name
        Player player = (Player) sender;
        String playerName = player.getName();

        int maxNumContracts = AssassinsPlugin.getSettings().getMaximumContracts();
        String revengeReason = AssassinsPlugin.getResources().getResource("assassins.command.revenge.reason");

        // get their current storage of revenges (this will only be those collected from the last time they were killed)
        PlayerContracts playerContracts = AssassinsPlugin.getStorage().getPlayerContracts(playerName);
        if (playerContracts != null) {

            List<Contract> revengeContracts = playerContracts.getRevengeContracts();
            if (revengeContracts.size() == 0) {
                MessageUtil.sendMessage(sender, "assassins.command.revenge.noneAvailable", ChatColor.RED);
                return false;
            }

            // convert each revenge contract into a new contract and place them onto the wanted list
            for (Contract contract : revengeContracts) {

                String requestorName = contract.getRequestPlayerName();

                // get the contracts (if any) against the creator of this contract
                PlayerContracts requestorContracts = AssassinsPlugin.getStorage().getPlayerContracts(requestorName);

                // check to see if they are under protection
                if (AssassinsPlugin.getStorage().isProtectedPlayer(requestorName)) {

                    MessageUtil.sendMessage(sender, "assassins.command.revenge.protectedPlayer", ChatColor.RED,
                            new MessageParameter("<player>", requestorName, ChatColor.AQUA));

                    continue;
                }

                // check to see if they've reached their limit on number of contracts
                if (requestorContracts != null && maxNumContracts > 0
                        && requestorContracts.getContracts().size() >= maxNumContracts) {

                    MessageUtil.sendMessage(sender, "assassins.command.revenge.maxContracts", ChatColor.RED,
                            new MessageParameter("<player>", requestorName, ChatColor.AQUA));

                    continue;
                }

                // create a contract against each person who had one on them
                boolean contractSubmitted =
                        AssassinsPlugin.getStorage().createContract(sender.getName(), requestorName,
                                contract.getPayment(), Type.revenge, revengeReason);

                if (contractSubmitted) {

                    MessageUtil.sendMessage(sender, "assassins.command.revenge.completed", ChatColor.GREEN,
                            new MessageParameter("<player>", requestorName, ChatColor.AQUA));

                } else {

                    MessageUtil.sendMessage(sender, "assassins.command.revenge.alreadyHasContract", ChatColor.RED);
                }
            }

            // clear out their list of revenge contracts
            playerContracts.clearRevengeContracts();

            // if they have no contracts left on them, then remove them from the list
            if (playerContracts.getContracts().size() == 0) {
                AssassinsPlugin.getStorage().removePlayerContracts(player.getName());
            }

            // save the changes
            AssassinsPlugin.getStorage().saveAll();

        } else {

            MessageUtil.sendMessage(sender, "assassins.command.revenge.noneAvailable", ChatColor.RED);
            return false;
        }

        return true;
    }

    @Override
    public HelpSegment help() {

        ResourcesConfig config = AssassinsPlugin.getResources();

        HelpMessage mainCommand = new HelpMessage();
        mainCommand.setCommand(getCommandType().toString());
        mainCommand.setArguments("");
        mainCommand.setDescription(config.getResource("assassins.command.revenge.help"));

        HelpSegment helpSegment = new HelpSegment(getCommandType());
        helpSegment.addEntry(mainCommand);

        return helpSegment;
    }
}
