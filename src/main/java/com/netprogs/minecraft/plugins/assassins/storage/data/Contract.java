package com.netprogs.minecraft.plugins.assassins.storage.data;

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

public class Contract {

    public enum Type {
        normal, revenge, auto
    }

    private transient Type contractTypeEnum;
    private String contractType;

    private Payment payment;
    private String playerName;
    private String requestPlayerName;
    private String reason;
    private long expiryDate;

    public void setContractType(Type contractType) {
        this.contractTypeEnum = contractType;
        this.contractType = contractType.toString();
    }

    public Type getContractType() {

        if (contractType == null) {
            contractType = Type.normal.toString();
        }

        if (contractTypeEnum == null) {
            contractTypeEnum = Type.valueOf(contractType);
        }

        return contractTypeEnum;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getRequestPlayerName() {
        return requestPlayerName;
    }

    public void setRequestPlayerName(String requestPlayerName) {
        this.requestPlayerName = requestPlayerName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getTimeRemaining() {

        long currentTime = System.currentTimeMillis();
        long expiryTime = getExpiryDate();
        long remaining = expiryTime - currentTime;
        return remaining;
    }

    public boolean isExpired() {

        long currentTime = System.currentTimeMillis();
        long remaining = getExpiryDate() - currentTime;

        if (remaining > 0) {
            return false;
        }
        return true;
    }
}
