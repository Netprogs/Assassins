package com.netprogs.minecraft.plugins.assassins.help;

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

public class HelpMessage implements IHelpEntry {

    private String command;
    private String arguments;
    private String description;

    public void setCommand(String command) {
        this.command = command;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String display() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(HelpBook.COMMAND_COLOR + command);

        if (!StringUtils.isEmpty(arguments)) {
            buffer.append(HelpBook.PARAMS_COLOR);
            buffer.append(" ");
            buffer.append(arguments);
        }

        buffer.append(HelpBook.DESCRIPTION_COLOR + " " + description);

        return buffer.toString();
    }
}
