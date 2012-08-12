package com.netprogs.minecraft.plugins.assassins.event;

import com.netprogs.minecraft.plugins.assassins.storage.data.PlayerContracts;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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

/**
 * Called when an assassin completes their contract and kills the required player.
 */
public class AssassinsContractCompletedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player assassin;
    private Player killed;
    private PlayerContracts contracts;

    public AssassinsContractCompletedEvent(Player assassin, Player killed, PlayerContracts contracts) {

        this.assassin = assassin;
        this.killed = killed;
        this.contracts = contracts;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getAssassin() {
        return assassin;
    }

    public Player getKilled() {
        return killed;
    }

    public PlayerContracts getPlayerContract() {
        return contracts;
    }
}
