package com.netprogs.minecraft.plugins.assassins.command;

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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer.WaitState;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandCancel;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandContracts;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandExpired;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandHelp;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandHunt;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandKill;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandProtect;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandRevenge;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandBlitz;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandTrack;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandView;
import com.netprogs.minecraft.plugins.assassins.command.dispatch.CommandWanted;
import com.netprogs.minecraft.plugins.assassins.command.exception.ArgumentsMissingException;
import com.netprogs.minecraft.plugins.assassins.command.exception.InvalidPermissionsException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotFoundException;
import com.netprogs.minecraft.plugins.assassins.command.exception.PlayerNotOnlineException;
import com.netprogs.minecraft.plugins.assassins.command.exception.SenderNotPlayerException;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;
import com.netprogs.minecraft.plugins.assassins.help.HelpBook;
import com.netprogs.minecraft.plugins.assassins.help.HelpPage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Dispatches all incoming commands off to their related {@link IPluginCommand} instance to handle.
 * @author Scott Milne
 */
public class PluginDispatcher implements CommandExecutor {

    private final Map<ICommandType, IPluginCommand> commands = new HashMap<ICommandType, IPluginCommand>();

    private JavaPlugin plugin;

    public PluginDispatcher(JavaPlugin plugin) {

        this.plugin = plugin;
        createCommandMap();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {

        // first thing we want to do is check for who's sending this request
        if (AssassinsPlugin.getSettings().isLoggingDebug()) {
            StringWriter argumentList = new StringWriter();
            for (String argument : arguments) {
                argumentList.append(argument);
                argumentList.append(" ");
            }
            AssassinsPlugin.logger().info("Incoming command: " + argumentList.toString());
        }

        try {

            // if nothing given, don't continue
            if (arguments.length == 0) {
                throw new ArgumentsMissingException();
            }

            // Grab the first argument, this should be our command.
            ICommandType requestedCommand = null;
            if (PluginCommandType.contains(arguments[0].toLowerCase())) {
                requestedCommand = PluginCommandType.valueOf(arguments[0].toLowerCase());
            }

            if (requestedCommand == null) {
                MessageUtil.sendMessage(sender, "plugin.error.unknownArguments", ChatColor.RED);
                return false;
            }

            // put the rest into a list
            List<String> commandArguments = new ArrayList<String>();
            for (int i = 1; i < arguments.length; i++) {
                commandArguments.add(arguments[i]);
            }

            // check to see if we need to wait for the user to respond to a question from us
            boolean processRequest = processWaitCommand(sender, requestedCommand);
            if (!processRequest) {

                // the user sent a command we're not waiting on, so cancel the command
                return false;
            }

            // process the rest of the commands
            if (commands.containsKey(requestedCommand)) {

                IPluginCommand pluginCommand = commands.get(requestedCommand);

                // try to run the command
                try {

                    pluginCommand.run(plugin, sender, commandArguments);

                } catch (SenderNotPlayerException exception) {

                    // If we're here, the command wasn't sent from a player and the command needed them to be one.
                    MessageUtil.sendSenderNotPlayerMessage(sender);

                } catch (ArgumentsMissingException exception) {

                    // If we're here, the command wasn't given enough information.
                    MessageUtil.sendUnknownArgumentsMessage(sender);

                } catch (InvalidPermissionsException exception) {

                    // If we're here, the sender requesting the command did not have permission to do so
                    MessageUtil.sendInvalidPermissionsMessage(sender);

                } catch (PlayerNotOnlineException exception) {

                    // If we're here, the sender requested an action with a player that was off-line
                    MessageUtil.sendPlayerNotOnlineMessage(sender, exception.getPlayerName());

                } catch (PlayerNotFoundException exception) {

                    // If we're here, the sender requested an action with a player that was not found online or offline
                    MessageUtil.sendPlayerNotFoundMessage(sender, exception.getPlayerName());
                }

                // we've handled this command in one form or another
                return true;
            }

            // Send all help messages if none matched
            HelpBook.sendHelpPage(sender, plugin.getName(), 1);

        } catch (ArgumentsMissingException exception) {

            // If we're here, the command wasn't given enough information.
            MessageUtil.sendMessage(sender, "plugin.error.unknownArguments", ChatColor.RED);
        }

        return false;
    }

    private void createCommandMap() {

        CommandHelp help = new CommandHelp();
        commands.put(PluginCommandType.help, help);

        CommandKill kill = new CommandKill();
        commands.put(PluginCommandType.kill, kill);

        CommandCancel cancel = new CommandCancel();
        commands.put(PluginCommandType.cancel, cancel);

        CommandWanted wanted = new CommandWanted();
        commands.put(PluginCommandType.wanted, wanted);

        CommandView view = new CommandView();
        commands.put(PluginCommandType.view, view);

        CommandExpired expired = new CommandExpired();
        commands.put(PluginCommandType.expired, expired);

        CommandHunt hunt = new CommandHunt();
        commands.put(PluginCommandType.hunt, hunt);

        CommandTrack track = new CommandTrack();
        commands.put(PluginCommandType.track, track);

        CommandBlitz blitz = new CommandBlitz();
        commands.put(PluginCommandType.blitz, blitz);

        CommandRevenge revenge = new CommandRevenge();
        commands.put(PluginCommandType.revenge, revenge);

        CommandContracts contracts = new CommandContracts();
        commands.put(PluginCommandType.contracts, contracts);

        CommandProtect protect = new CommandProtect();
        commands.put(PluginCommandType.protect, protect);

        //
        // Help Pages
        //

        HelpPage baseHelpPage = new HelpPage();
        baseHelpPage.addSegment(help.help());
        baseHelpPage.addSegment(wanted.help());
        baseHelpPage.addSegment(kill.help());
        baseHelpPage.addSegment(revenge.help());
        baseHelpPage.addSegment(view.help());
        baseHelpPage.addSegment(hunt.help());
        baseHelpPage.addSegment(track.help());
        baseHelpPage.addSegment(blitz.help());
        baseHelpPage.addSegment(contracts.help());
        baseHelpPage.addSegment(cancel.help());
        baseHelpPage.addSegment(expired.help());
        baseHelpPage.addSegment(protect.help());

        HelpBook.addPage(baseHelpPage);
    }

    private boolean processWaitCommand(CommandSender sender, ICommandType requestedCommand) {

        Logger logger = AssassinsPlugin.logger();

        // first we want to check to see if we need to process any waiting commands
        // if the user is in a waiting state, they cannot do any other commands until they respond

        if ((sender instanceof Player)) {

            Player player = (Player) sender;

            // if the user is in a waiting state, then check to see if this requested command will handle it
            PluginPlayer pluginPlayer = AssassinsPlugin.getStorage().getPlayer(player);
            if (pluginPlayer != null && pluginPlayer.getWaitState() != null
                    && pluginPlayer.getWaitState() != WaitState.notWaiting) {

                if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                    logger.info("WaitState: " + pluginPlayer.getWaitState());
                    logger.info("WaitCommand: " + pluginPlayer.getWaitCommand());
                    logger.info("Requested Command: " + requestedCommand);
                }

                // get the command that is being requested and check to see if it is a wait command
                IPluginCommand pluginCommand = commands.get(requestedCommand);
                if (pluginCommand instanceof IWaitCommand) {

                    // we got a wait command, so now let's check to see if it wants to handle the command
                    boolean isValidWaitResponse =
                            ((IWaitCommand) pluginCommand).isValidWaitReponse(sender, pluginPlayer);

                    if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                        logger.info("Requested command is an IWaitCommand");
                        logger.info("isValidWaitResponse: " + isValidWaitResponse);
                    }

                    return isValidWaitResponse;

                } else {

                    if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                        logger.info("Requested command is NOT an IWaitCommand. Using person WaitCommand for help page.");
                    }

                    // The command requested wasn't an IWaitCommand, so let's use the person's getWaitCommand() and look
                    // it up so we can provide a help page.
                    if (pluginPlayer.getWaitCommand() != null) {

                        IPluginCommand waitCommand = commands.get(pluginPlayer.getWaitCommand());

                        if (waitCommand instanceof IWaitCommand) {
                            ((IWaitCommand) waitCommand).displayWaitHelp(sender);
                        }
                    }

                    // cancel the command request, we didn't get anything useful
                    return false;
                }
            }
        }

        // player isn't waiting on anything, continue on
        return true;
    }
}
