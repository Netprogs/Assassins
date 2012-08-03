package com.netprogs.minecraft.plugins.assassins.storage.json;

import java.util.Map;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

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

public class JsonDataManager {

    private JsonStorage storage;

    public JsonDataManager() {

        // create and run the JSON configuration loader
        storage = new JsonStorage(AssassinsPlugin.instance.getDataFolder() + "/storage.json");
        storage.loadConfig();
    }

    public boolean isProtectedPlayer(String playerName) {
        return storage.getStorage().getProtectedPlayers().contains(playerName.toLowerCase());
    }

    public void addProtectedPlayer(String playerName) {
        storage.getStorage().getProtectedPlayers().add(playerName.toLowerCase());
        storage.saveConfig();
    }

    public void removeProtectedPlayer(String playerName) {
        storage.getStorage().getProtectedPlayers().remove(playerName.toLowerCase());
        storage.saveConfig();
    }

    public Map<String, PlayerContracts> getPlayerContracts() {
        return storage.getStorage().getPlayerContracts();
    }

    public void saveAll() {
        storage.saveConfig();
    }

    public PlayerContracts getPlayerContracts(String playerName) {
        return storage.getStorage().getPlayerContracts().get(playerName.toLowerCase());
    }

    public void removePlayerContracts(String playerName) {
        storage.getStorage().getPlayerContracts().remove(playerName.toLowerCase());
        storage.saveConfig();
    }

    public boolean alreadyHasContractOn(String requestPlayerName, String playerName) {

        if (storage.getStorage().getPlayerContracts().containsKey(playerName.toLowerCase())) {

            PlayerContracts playerContracts = storage.getStorage().getPlayerContracts().get(playerName.toLowerCase());
            for (Contract contract : playerContracts.getContracts()) {
                if (contract.getRequestPlayerName().equalsIgnoreCase(requestPlayerName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addContract(String playerName, Contract contract) {

        // if there is no list for the player yet, add an empty one now
        if (!storage.getStorage().getPlayerContracts().containsKey(playerName.toLowerCase())) {

            PlayerContracts playerContracts = new PlayerContracts();
            playerContracts.setPlayerName(playerName);
            storage.getStorage().getPlayerContracts().put(playerName.toLowerCase(), playerContracts);
        }

        // add the contract to their list
        PlayerContracts playerContracts = storage.getStorage().getPlayerContracts().get(playerName.toLowerCase());
        playerContracts.getContracts().add(contract);
        storage.saveConfig();
    }

    public void removeContract(String playerName, Contract contract) {

        // removes the contract for the player
        PlayerContracts playerContracts = storage.getStorage().getPlayerContracts().get(playerName.toLowerCase());
        playerContracts.getContracts().remove(contract);

        // if the last one, remove the list
        if (playerContracts.getContracts().size() == 0) {
            storage.getStorage().getPlayerContracts().remove(playerName.toLowerCase());
        }

        storage.saveConfig();
    }
}
