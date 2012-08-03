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

import com.netprogs.minecraft.plugins.assassins.io.JsonConfiguration;

public class SettingsConfig extends JsonConfiguration<Settings> {

    public SettingsConfig(String configFileName) {
        super(configFileName);
    }

    public boolean isLoggingDebug() {
        return getDataObject().isLoggingDebug();
    }

    public int getMaximumContracts() {
        return getDataObject().getMaximumContracts();
    }

    public int getContractExpireTime() {
        return getDataObject().getContractExpireTime();
    }

    public int getAssassinExpireTime() {
        return getDataObject().getAssassinExpireTime();
    }

    public int getLocationTrackingAdjustment() {
        return getDataObject().getLocationTrackingAdjustment();
    }

    public AutoContractor getAutoContractor() {
        return getDataObject().getAutoContractor();
    }

    public Blitz getBlitz() {
        return getDataObject().getBlitz();
    }
}
