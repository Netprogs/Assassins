package com.netprogs.minecraft.plugins.assassins.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.mysql.jdbc.StringUtils;
import com.netprogs.minecraft.plugins.assassins.PluginPlayer;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.SettingsConfig;
import com.netprogs.minecraft.plugins.assassins.storage.data.Contract;
import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;
import com.netprogs.minecraft.plugins.assassins.storage.json.JsonDataManager;
import com.netprogs.minecraft.plugins.assassins.view.ContractWantedItem;

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

public class PluginStorage {

    private final Logger logger = Logger.getLogger("Minecraft");

    private static final PluginStorage SINGLETON = new PluginStorage();

    public static PluginStorage getInstance() {
        return SINGLETON;
    }

    private PluginStorage() {
    }

    private JsonDataManager dataManager = new JsonDataManager();
    private Map<String, PluginPlayer> loadedPlayerMap = new HashMap<String, PluginPlayer>();

    public PluginPlayer getPlayer(Player player) {

        if (!loadedPlayerMap.containsKey(player.getName())) {

            PluginPlayer wrapper = new PluginPlayer(player);
            loadedPlayerMap.put(player.getName(), wrapper);
            return wrapper;
        }

        return loadedPlayerMap.get(player.getName());
    }

    public void removePlayer(Player player) {

        loadedPlayerMap.remove(player.getName());
    }

    public boolean isProtectedPlayer(String playerName) {
        return dataManager.isProtectedPlayer(playerName);
    }

    public void addProtectedPlayer(String playerName) {
        dataManager.addProtectedPlayer(playerName);
    }

    public void removeProtectedPlayer(String playerName) {
        dataManager.removeProtectedPlayer(playerName);
    }

    public boolean createContract(String requestPlayerName, String playerName, double payment, String reason) {

        if (dataManager.alreadyHasContractOn(requestPlayerName, playerName)) {
            return false;
        }

        int expiryTimeMinutes = PluginConfig.getInstance().getConfig(SettingsConfig.class).getContractExpireTime();
        long currentTime = System.currentTimeMillis();
        long expiryDate = currentTime + (expiryTimeMinutes * 60 * 1000);

        if (PluginConfig.getInstance().getConfig(SettingsConfig.class).isLoggingDebug()) {

            logger.info("expiryTimeMinutes: " + expiryTimeMinutes);
            logger.info("currentTime: " + currentTime);
            logger.info("expiryDate: " + expiryDate);
        }

        Contract contract = new Contract();
        contract.setPayment(payment);
        contract.setReason(reason);
        contract.setPlayerName(playerName);
        contract.setRequestPlayerName(requestPlayerName);
        contract.setExpiryDate(expiryDate);

        dataManager.addContract(playerName, contract);

        return true;
    }

    public void removePlayerContracts(String playerName) {
        dataManager.removePlayerContracts(playerName);
    }

    public Contract removeContract(String requestPlayerName, String playerName) {

        PlayerContracts playerContracts = dataManager.getPlayerContracts(playerName);
        if (playerContracts != null && playerContracts.getContracts().size() > 0) {

            // make sure the contract isn't active
            if (!StringUtils.isNullOrEmpty(playerContracts.getAssassinPlayerName())) {
                return null;
            }

            for (Contract contract : playerContracts.getContracts()) {

                // remove them
                if (contract.getRequestPlayerName().equalsIgnoreCase(requestPlayerName)) {
                    dataManager.removeContract(playerName, contract);
                    return contract;
                }
            }
        }

        return null;
    }

    public void saveAll() {
        dataManager.saveAll();
    }

    public List<ContractWantedItem> getContracts() {

        List<ContractWantedItem> contractItems = new ArrayList<ContractWantedItem>();

        Map<String, PlayerContracts> contractMap = dataManager.getPlayerContracts();

        long oldestExpiryTime = -1;

        for (String playerName : contractMap.keySet()) {

            PlayerContracts playerContracts = contractMap.get(playerName);

            // get the list of non-expired contracts
            List<Contract> contracts = getContracts(playerName);
            if (contracts.size() > 0) {

                // go through their list of contracts and get the total
                double totalPayout = 0;
                for (Contract contract : contracts) {

                    totalPayout += contract.getPayment();

                    if (contract.getExpiryDate() < oldestExpiryTime || oldestExpiryTime == -1) {
                        oldestExpiryTime = contract.getExpiryDate();
                    }
                }

                ContractWantedItem listItem = new ContractWantedItem();
                listItem.setNumberOfContracts(contracts.size());
                listItem.setTotalPayment(totalPayout);
                listItem.setOldestExpiryDate(oldestExpiryTime);
                listItem.setPlayerName(playerName);
                listItem.setHunterPlayerName(playerContracts.getAssassinPlayerName());
                listItem.setHunterTimeLimit(playerContracts.getAssassinTimeLimit());
                contractItems.add(listItem);
            }
        }

        // now sort them so their oldest expiry date is at top
        Collections.sort(contractItems, new Comparator<ContractWantedItem>() {
            public int compare(ContractWantedItem a, ContractWantedItem b) {
                if (a.getOldestExpiryDate() < b.getOldestExpiryDate()) {
                    return -1;
                } else if (a.getOldestExpiryDate() == b.getOldestExpiryDate()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        return contractItems;
    }

    public PlayerContracts getPlayerContracts(String playerName) {

        // adjust the list so they only get the one's that are not expired
        PlayerContracts playerContracts = dataManager.getPlayerContracts(playerName);
        if (playerContracts != null) {
            List<Contract> contracts = playerContracts.getContracts();

            Iterator<Contract> iter = contracts.iterator();
            while (iter.hasNext()) {
                if (iter.next().isExpired()) {
                    iter.remove();
                }
            }
        }
        return playerContracts;
    }

    public List<Contract> getContracts(String playerName) {

        List<Contract> activeContracts = new ArrayList<Contract>();
        PlayerContracts playerContracts = dataManager.getPlayerContracts(playerName);

        if (playerContracts != null && playerContracts.getContracts().size() > 0) {

            // go through their list of contracts and get the total and find the oldest one
            for (Contract contract : playerContracts.getContracts()) {

                // make sure they haven't expired
                if (!contract.isExpired()) {
                    activeContracts.add(contract);
                }
            }
        }

        return activeContracts;
    }

    public Map<String, PlayerContracts> getHunterContracts(String hunterPlayerName) {

        Map<String, PlayerContracts> hunterContractMap = new HashMap<String, PlayerContracts>();

        // go through the list of contracts and pull out those that the hunter is assigned to
        Map<String, PlayerContracts> contractMap = dataManager.getPlayerContracts();
        for (String playerName : contractMap.keySet()) {

            PlayerContracts playerContracts = contractMap.get(playerName);
            if (playerContracts.getAssassinPlayerName() != null
                    && playerContracts.getAssassinPlayerName().equalsIgnoreCase(hunterPlayerName)) {

                // check to make sure your timer is still valid
                if (playerContracts.getAssassinTimeRemaining() > 0) {
                    hunterContractMap.put(playerName, playerContracts);
                }
            }
        }

        return hunterContractMap;
    }

    public List<Contract> getExpiredContracts(String requestPlayerName) {

        List<Contract> expiredContracts = new ArrayList<Contract>();

        Map<String, PlayerContracts> contracts = dataManager.getPlayerContracts();
        for (String playerName : contracts.keySet()) {

            // get the list of contracts
            PlayerContracts playerContracts = dataManager.getPlayerContracts(playerName);
            if (playerContracts != null && playerContracts.getContracts().size() > 0) {

                // they can only collect an expired contract if it's not being hunted
                if (playerContracts.getAssassinTimeRemaining() < 0) {

                    // go through their list of contracts and get the expired one's that match the request player
                    for (Contract contract : playerContracts.getContracts()) {
                        if (contract.getRequestPlayerName().equalsIgnoreCase(requestPlayerName)) {
                            if (contract.isExpired()) {
                                expiredContracts.add(contract);
                            }
                        }
                    }
                }
            }
        }

        return expiredContracts;
    }
}
