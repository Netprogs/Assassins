package com.netprogs.minecraft.plugins.assassins.config.settings;

import java.util.List;

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

public class AutoContractor {

    // This allows you to enable/disable auto contracts.
    private boolean enabled;

    // The maximum number of auto contracts allowed to be listed at once.
    private int maximumContracts;

    // This is the list of contracts you wish to have people checked against upon login.
    private List<AutoContract> contracts;

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaximumContracts() {
        return maximumContracts;
    }

    public List<AutoContract> getContracts() {
        return contracts;
    }
}
