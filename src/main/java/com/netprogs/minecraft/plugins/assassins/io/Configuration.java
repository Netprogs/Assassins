package com.netprogs.minecraft.plugins.assassins.io;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;

/**
 * <pre>
 * Abstract class for managing loading/saving of configuration data.
 *  
 * This class handles the following:
 *  - Determines if this is the first time being called and extracts the configuration file from the jar.
 *  - Checks to see if a default data object was provided and uses that instead of jar extraction.
 *  
 *  This class cannot be used directly and should only be used to create file type specific variations.
 * </pre>
 * @param <T> Generic bean for the configuration to place loaded data into.
 */
public abstract class Configuration<T> {

    private boolean firstRun;
    private boolean copyDefaults;
    private File configFile;
    private Class<T> classObject;

    private T dataObject;
    private T defaultDataObject;

    @SuppressWarnings("unchecked")
    private void createClassObject() {

        // get the runtime class from the sub-class instance for the generic type being used
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        classObject = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    protected Configuration(String configFileName, boolean ignoreExtraction) {
        init(configFileName, ignoreExtraction);
    }

    /**
     * Constructor for creating a configuration.
     * @param configFileName The location of the configuration file to be placed when extracted/saved.
     */
    protected Configuration(String configFileName) {
        init(configFileName, false);
    }

    private void init(String configFileName, boolean ignoreExtraction) {

        createClassObject();

        this.defaultDataObject = null;
        this.configFile = new File(configFileName);

        this.firstRun = false;

        if (!configFile.exists()) {

            // create the directories
            configFile.getParentFile().mkdirs();

            if (!ignoreExtraction) {
                this.firstRun = true;
            }
        }
    }

    /**
     * <pre>
     * Loads the configuration data into the data object.
     * The loadConfig() method calls preLoad(), load() and postLoad(). 
     * These methods should be overridden as needed by your sub-class.
     * </pre>
     */
    public final void loadConfig() {

        // extract the configuration data into a file if needed
        extractConfiguration();

        // create the config file from the jar
        preLoad();

        load();

        // called to provide
        postLoad();
    }

    /**
     * <pre>
     * Saves the configuration data object to the file type determined by the sub-class.
     * The saveConfig() method calls preSave(), save() and postSave(). 
     * These methods should be overridden as needed by your sub-class.
     * </pre>
     */
    public final void saveConfig() {

        preSave();

        save();

        postSave();
    }

    /**
     * <pre>
     * Does pre-loading work before the actual load is requested.
     * </pre>
     */
    private void extractConfiguration() {

        // if the configuration file doesn't exist, we need to create or extract it
        if (isFirstRun() && getCopyDefaults()) {

            // if we should be using the default, then create the file, set the object and save it
            try {

                configFile.createNewFile();
                setDataObject(defaultDataObject);
                saveConfig();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else if (isFirstRun()) {

            // we just want to pull it from out jar instead
            extractFromJar();
        }
    }

    /**
     * Override to provide any pre-loading work that needs to be done.
     */
    protected void preLoad() {

    }

    /**
     * Override this method to provide the file type specific requirements for loading the configuration data.
     */
    protected abstract void load();

    /**
     * Override to provide any post-loading work that needs to be done.
     */
    protected void postLoad() {

        this.firstRun = false;
    }

    /**
     * Override to provide any pre-saving work.
     */
    protected void preSave() {

    }

    /**
     * Override this method to provide the file type specific requirements for saving the configuration data.
     */
    protected abstract void save();

    /**
     * Override to provide any post-saving work.
     */
    protected void postSave() {

    }

    /**
     * Reloads the configuration data by calling {@link #saveConfig} then {@link #loadConfig}.
     */
    public final void reloadConfig() {
        try {

            // saveConfig();

            setDataObject(null);
            loadConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setDataObject(T dataObject) {
        this.dataObject = dataObject;
    }

    protected T getDataObject() {
        return dataObject;
    }

    private void extractFromJar() {

        try {

            // grab the file from our jar
            InputStream inputStream = classObject.getClassLoader().getResourceAsStream(configFile.getName());

            // open the config file and place the contents of our initial configuration into it
            FileWriter outputStream = new FileWriter(configFile);

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }

            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Class<T> getClassObject() {
        return classObject;
    }

    protected File getConfigFile() {
        return configFile;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setDefaultDataObject(T defaultDataObject) {
        this.defaultDataObject = defaultDataObject;
    }

    public boolean getCopyDefaults() {
        return copyDefaults;
    }

    public void setCopyDefaults(boolean copyDefaults) {
        this.copyDefaults = copyDefaults;
    }
}
