package com.netprogs.minecraft.plugins.assassins.help;

import java.util.ArrayList;
import java.util.List;

import com.netprogs.minecraft.plugins.assassins.command.ICommandType;

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

public class HelpSegment {

    private ICommandType commandType;
    private List<IHelpEntry> entries = new ArrayList<IHelpEntry>();
    private String title;

    public HelpSegment(ICommandType commandType) {
        this.title = StringUtils.EMPTY;
        this.commandType = commandType;
    }

    public String getTitle() {
        return title;
    }

    public List<IHelpEntry> getEntries() {
        return entries;
    }

    public void addEntry(IHelpEntry entry) {
        this.entries.add(entry);
    }

    public ICommandType getCommandType() {
        return commandType;
    }
}
