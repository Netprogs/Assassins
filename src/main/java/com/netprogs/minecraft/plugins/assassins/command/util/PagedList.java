package com.netprogs.minecraft.plugins.assassins.command.util;

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

public class PagedList {

    public static class PagedItems<T> {
        public List<T> items;
        public int numFullPages;
    }

    public static <U extends Object> PagedItems<U> getPagedList(List<U> items, int pageNumber, int maxPerPage) {

        PagedItems<U> pagedItems = new PagedItems<U>();

        // get the number of pages available
        int numFullPages = (items.size() / maxPerPage);
        int numRemainder = (items.size() % maxPerPage);
        if (numRemainder > 0) {
            numFullPages += 1;
        }

        // set the number of pages
        pagedItems.numFullPages = numFullPages;

        // determine the starting point. If it's past the current number of items, tell them it's invalid.
        int startIndex = ((pageNumber - 1) * maxPerPage);
        if (startIndex > items.size()) {
            return pagedItems;
        }

        // get the end point, if it's past the size, then make it the full length
        int endIndex = (pageNumber * maxPerPage);
        if (endIndex > items.size()) {
            endIndex = items.size();
        }

        // System.out.println("numFullPages: " + numFullPages);
        // System.out.println("startIndex: " + startIndex);
        // System.out.println("endIndex: " + endIndex);

        // grab the sub list for displaying
        pagedItems.items = items.subList(startIndex, endIndex);

        return pagedItems;
    }
}
