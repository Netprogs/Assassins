package com.netprogs.minecraft.plugins.assassins.storage.json;

import com.google.gson.GsonBuilder;
import com.netprogs.minecraft.plugins.assassins.io.JsonConfiguration;
import com.netprogs.minecraft.plugins.assassins.io.JsonPaymentAdapter;
import com.netprogs.minecraft.plugins.assassins.storage.data.Payment;
import com.netprogs.minecraft.plugins.assassins.storage.data.Storage;

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

public class JsonStorage extends JsonConfiguration<Storage> {

    public JsonStorage(String configFileName) {
        super(configFileName);
    }

    public Storage getStorage() {
        return getDataObject();
    }

    @Override
    protected void registerTypeAdapters(GsonBuilder builder) {

        // register the Message interface
        builder.registerTypeAdapter(Payment.class, new JsonPaymentAdapter()).create();
    }
}
