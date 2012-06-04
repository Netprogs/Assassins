package com.netprogs.minecraft.plugins.assassins.storage.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class Storage {

    // Map<PlayerName, PlayerContracts>
    private Map<String, PlayerContracts> playerContracts = new HashMap<String, PlayerContracts>();

    private List<String> protectedPlayers = new ArrayList<String>();

    public List<String> getProtectedPlayers() {
        return protectedPlayers;
    }

    public Map<String, PlayerContracts> getPlayerContracts() {
        return playerContracts;
    }
}
