package com.netprogs.minecraft.plugins.assassins.config;

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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JsonConfiguration<T> extends Configuration<T> {

    private Gson json;

    protected JsonConfiguration(String configFileName) {
        super(configFileName);
        init();
    }

    protected JsonConfiguration(String configFileName, boolean ignoreExtraction) {
        super(configFileName, ignoreExtraction);
        init();
    }

    private void init() {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.disableHtmlEscaping();
        builder.serializeNulls();

        // allow sub-classes to register type adapters
        registerTypeAdapters(builder);

        // initialize the json instance
        json = builder.create();
    }

    protected void registerTypeAdapters(GsonBuilder builder) {

    }

    /**
     * Implements {@link Configuration#load()} to process a JSON based file load.
     */
    protected synchronized void load() {

        InputStream inputStream = null;
        InputStreamReader reader = null;

        try {

            inputStream = new FileInputStream(getConfigFile());
            reader = new InputStreamReader(inputStream);

            setDataObject(json.fromJson(reader, getClassObject()));

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            try {

                if (reader != null) {
                    reader.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Implements {@link Configuration#save()} to process a JSON based file save.
     */
    protected synchronized void save() {

        FileWriter fstream = null;
        BufferedWriter out = null;

        try {

            String jsonOutput = json.toJson(getDataObject());

            fstream = new FileWriter(getConfigFile());
            out = new BufferedWriter(fstream);

            out.write(jsonOutput);
            out.close();
            fstream.close();

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            try {

                if (fstream != null) {
                    fstream.close();
                }

                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
