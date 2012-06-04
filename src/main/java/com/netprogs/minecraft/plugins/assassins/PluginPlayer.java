package com.netprogs.minecraft.plugins.assassins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netprogs.minecraft.plugins.assassins.command.ICommandType;
import com.netprogs.minecraft.plugins.assassins.command.IWaitData;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

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

public class PluginPlayer {

    public enum WaitState {
        notWaiting
    }

    private Player player;

    private ICommandType waitCommand;
    private WaitState waitState;
    private IWaitData waitData;

    // Map<PlayerName, PlayerContracts>
    private Map<String, PlayerContracts> playerContracts = new HashMap<String, PlayerContracts>();

    public PluginPlayer(Player player) {

        this.player = player;

        loadContracts();
    }

    public void setWaitCommand(ICommandType waitCommand) {
        this.waitCommand = waitCommand;
    }

    public ICommandType getWaitCommand() {
        return waitCommand;
    }

    public WaitState getWaitState() {
        return waitState;
    }

    public void setWaitState(WaitState waitState) {
        this.waitState = waitState;
    }

    @SuppressWarnings("unchecked")
    public <U extends IWaitData> U getWaitData() {
        return (U) waitData;
    }

    public <U extends IWaitData> void setWaitData(U waitData) {
        this.waitData = waitData;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerContracts getPlayerContracts(String playerName) {

        // only return the object if it hasn't expired yet
        PlayerContracts contracts = playerContracts.get(playerName);
        if (contracts != null && contracts.getAssassinTimeRemaining() > 0) {
            return contracts;
        }

        return null;
    }

    public List<PlayerContracts> getSortedContracts() {

        List<PlayerContracts> contractList = new ArrayList<PlayerContracts>();

        for (String playerName : playerContracts.keySet()) {
            PlayerContracts playerContracts = getPlayerContracts(playerName);
            if (playerContracts != null && playerContracts.getContracts().size() > 0) {

                // check to make sure your timer is still valid
                if (playerContracts.getAssassinTimeRemaining() > 0) {
                    contractList.add(playerContracts);
                }
            }
        }

        // now sort them so their oldest expiry date is at top
        Collections.sort(contractList, new Comparator<PlayerContracts>() {
            public int compare(PlayerContracts a, PlayerContracts b) {
                if (a.getAssassinTimeRemaining() < b.getAssassinTimeRemaining()) {
                    return -1;
                } else if (a.getAssassinTimeRemaining() == b.getAssassinTimeRemaining()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        return contractList;
    }

    public void addPlayerContracts(String playerName, PlayerContracts playerContacts) {

        // if new one's are added later, they're still added to the PlayerContracts object so we'll have them
        playerContracts.put(playerName, playerContacts);
    }

    public void removeContracts(String playerName) {

        // remove from our list
        playerContracts.remove(playerName);

        // remove from the data source
        PluginStorage.getInstance().removePlayerContracts(playerName);
    }

    private void loadContracts() {

        if (playerContracts.isEmpty()) {
            playerContracts = PluginStorage.getInstance().getHunterContracts(player.getName());
        }
    }

    public boolean isProtectedPlayer() {
        return PluginStorage.getInstance().isProtectedPlayer(player.getName());
    }
}
