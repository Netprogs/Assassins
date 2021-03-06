package com.netprogs.minecraft.plugins.assassins.config.settings;

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

public class Settings {

    // The maximum number of contracts a single player can have on them. -1 for unlimited.
    private int maximumContracts;

    // The amount of time (in minutes) that a contract is available for hunting. Default 1 day.
    private int contractExpireTime;

    // The amount of time (in minutes) an assassin has to kill player before the contract goes back to the wanted list.
    private int assassinExpireTime;

    // The distance (in blocks) to round to for giving "estimated" location of a player being hunted
    private int locationTrackingAdjustment;

    // The blitz settings
    private Blitz blitz = new Blitz();

    // The Auto Contract settings
    private AutoContractor autoContractor = new AutoContractor();

    private boolean loggingDebug;

    public boolean isLoggingDebug() {
        return loggingDebug;
    }

    public int getMaximumContracts() {
        return maximumContracts;
    }

    public int getContractExpireTime() {
        return contractExpireTime;
    }

    public int getAssassinExpireTime() {
        return assassinExpireTime;
    }

    public int getLocationTrackingAdjustment() {
        return locationTrackingAdjustment;
    }

    public AutoContractor getAutoContractor() {
        return autoContractor;
    }

    public Blitz getBlitz() {
        return blitz;
    }
}
