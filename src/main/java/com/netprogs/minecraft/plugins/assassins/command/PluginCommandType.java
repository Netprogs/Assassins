package com.netprogs.minecraft.plugins.assassins.command;

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

public enum PluginCommandType implements ICommandType {

    help, kill, cancel, wanted, view, track, hunt, expired, contracts, protect, revenge, blitz;

    public static boolean contains(String type) {
        PluginCommandType[] values = PluginCommandType.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].toString().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
