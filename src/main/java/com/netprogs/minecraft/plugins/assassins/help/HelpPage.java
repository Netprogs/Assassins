package com.netprogs.minecraft.plugins.assassins.help;

import java.util.ArrayList;
import java.util.List;

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

public class HelpPage {

    private String title;

    public HelpPage() {
        this.title = StringUtils.EMPTY;
    }

    public HelpPage(String title) {
        this.title = title;
    }

    private List<HelpSegment> segments = new ArrayList<HelpSegment>();

    public List<HelpSegment> getSegments() {
        return segments;
    }

    public void addSegment(HelpSegment segment) {
        this.segments.add(segment);
    }

    public String getTitle() {
        return title;
    }
}
