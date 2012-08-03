package com.netprogs.minecraft.plugins.assassins.view;

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

public class ContractWantedItem {

    private int numberOfContracts;
    private String playerName;
    private String hunterPlayerName;
    private long hunterTimeLimit;
    private long oldestExpiryDate;

    public int getNumberOfContracts() {
        return numberOfContracts;
    }

    public void setNumberOfContracts(int numberOfContracts) {
        this.numberOfContracts = numberOfContracts;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getHunterPlayerName() {
        return hunterPlayerName;
    }

    public void setHunterPlayerName(String hunterPlayerName) {
        this.hunterPlayerName = hunterPlayerName;
    }

    public long getHunterTimeLimit() {
        return hunterTimeLimit;
    }

    public void setHunterTimeLimit(long hunterTimeLimit) {
        this.hunterTimeLimit = hunterTimeLimit;
    }

    public long getOldestExpiryDate() {
        return oldestExpiryDate;
    }

    public void setOldestExpiryDate(long oldestExpiryDate) {
        this.oldestExpiryDate = oldestExpiryDate;
    }

    public long getOldestExpiryTimeRemaining() {
        return getTimeRemaining(getOldestExpiryDate());
    }

    public boolean isAvailable() {
        return !(getHunterTimeRemaining() > 0 && StringUtils.isNotBlank(hunterPlayerName));
    }

    public long getHunterTimeRemaining() {
        return getTimeRemaining(getHunterTimeLimit());
    }

    private long getTimeRemaining(long expiryTime) {

        long currentTime = System.currentTimeMillis();
        long remaining = expiryTime - currentTime;
        return remaining;
    }
}
