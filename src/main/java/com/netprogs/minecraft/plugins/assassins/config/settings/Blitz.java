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

public class Blitz {

    // The duration for how long blitz will last. (seconds)
    private int duration;

    // The cool down period that must pass before they can use it again. (seconds)
    private int cooldown;

    // The number of blocks the player must be within of their prey in order to activate a blitz attack
    private int proximity;

    // If you want to allow them to turn invisible
    private boolean allowInvisible;

    // If you want to allow the player to have higher moving speed
    private boolean allowSpeed;

    // If you want to allow the player to have higher jump
    private boolean allowJump;

    // If you want to allow them to have the side effect of slowness applied.
    private boolean allowSlow;

    // If you want to allow them to have the side effect of blindness applied.
    private boolean allowBlindness;

    // If you want to allow them to have the side effect of confusion applied.
    private boolean allowConfusion;

    // If you want to allow better compass tracking during blitz, set it here. 0 means exact location.
    private int locationTrackingAdjustment;

    public int getDuration() {
        return duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean isAllowInvisible() {
        return allowInvisible;
    }

    public boolean isAllowSpeed() {
        return allowSpeed;
    }

    public boolean isAllowJump() {
        return allowJump;
    }

    public int getLocationTrackingAdjustment() {
        return locationTrackingAdjustment;
    }

    public int getProximity() {
        return proximity;
    }

    public boolean isAllowSlow() {
        return allowSlow;
    }

    public boolean isAllowBlindness() {
        return allowBlindness;
    }

    public boolean isAllowConfusion() {
        return allowConfusion;
    }
}
