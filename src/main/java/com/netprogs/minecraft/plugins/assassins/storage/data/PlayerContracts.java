package com.netprogs.minecraft.plugins.assassins.storage.data;

import java.util.ArrayList;
import java.util.List;

import com.netprogs.minecraft.plugins.assassins.storage.data.Contract.Type;

import org.apache.commons.lang.StringUtils;

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

public class PlayerContracts {

    private String playerName;
    private String assassinPlayerName;
    private long assassinTimeLimit;

    private List<Contract> contracts = new ArrayList<Contract>();
    private List<Contract> revengeContracts = new ArrayList<Contract>();

    public List<Contract> getContracts() {
        return contracts;
    }

    public String getAssassinPlayerName() {
        return assassinPlayerName;
    }

    public void setAssassinPlayerName(String assassinPlayerName) {
        this.assassinPlayerName = assassinPlayerName;
    }

    public long getAssassinTimeLimit() {
        return assassinTimeLimit;
    }

    public void setAssassinTimeLimit(long assassinTimeLimit) {
        this.assassinTimeLimit = assassinTimeLimit;
    }

    public long getAssassinTimeRemaining() {

        long currentTime = System.currentTimeMillis();
        long expiryTime = getAssassinTimeLimit();
        long remaining = expiryTime - currentTime;
        return remaining;
    }

    public boolean isAvailable() {
        return !(getAssassinTimeRemaining() > 0 && StringUtils.isNotBlank(assassinPlayerName));
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean storeContractsForRevenge() {

        // clear out the revenge list so we're only tracking it once per assassination
        revengeContracts.clear();

        // go through each one and make sure it's not a double revenge or an auto-contract
        for (Contract contract : contracts) {
            if (contract.getContractType() != Type.revenge && contract.getContractType() != Type.auto) {
                revengeContracts.add(contract);
            }
        }

        // clear out the contract list
        contracts.clear();

        // return the flag saying if any were stored
        return revengeContracts.size() > 0;
    }

    public List<Contract> getRevengeContracts() {
        return revengeContracts;
    }

    public void clearRevengeContracts() {
        revengeContracts.clear();
    }
}
