package com.netprogs.minecraft.plugins.assassins;

/*
 * Copyright (C) 2012 Scott Milne
 * 
 * "Assassins" is a Craftbukkit Minecraft server modification plug-in. 
 * "Assassins" allows players to become and/or hire "assassins" on their servers.
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.command.PluginDispatcher;
import com.netprogs.minecraft.plugins.assassins.command.util.TimerManager;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.SettingsConfig;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;
import com.netprogs.minecraft.plugins.assassins.listener.AutoContractListener;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerDamageListener;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerDeathListener;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerQuitListener;
import com.netprogs.minecraft.plugins.assassins.storage.PluginStorage;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AssassinsPlugin extends JavaPlugin {

    // expose the instance of this class as a global so we can better access it's methods
    private static AssassinsPlugin instance;

    // used for sending completely anonymous data to http://mcstats.org for usage tracking
    private Metrics metrics;

    // used to hold all the plug-in settings
    private SettingsConfig settingsConfig;

    // used to hold all the plug-in resources
    private ResourcesConfig resourcesConfig;

    // used to access the data storage
    private PluginStorage storage;

    // used to access Vault
    private VaultIntegration vault;

    // used to manage command timers
    private TimerManager timerManager;

    public AssassinsPlugin() {

        instance = this;
    }

    public void onEnable() {

        // report that this plug in is being loaded
        PluginDescriptionFile pdfFile = getDescription();

        // create the settings configuration object
        settingsConfig = new SettingsConfig(getDataFolder() + "/config.json");
        settingsConfig.loadConfig();

        // create the resources configuration object
        resourcesConfig = new ResourcesConfig(getDataFolder() + "/resources.json");
        resourcesConfig.loadConfig();

        // create the data storage object
        storage = new PluginStorage();

        // create the vault integration object
        vault = new VaultIntegration(this, "assassin", settingsConfig.isLoggingDebug());
        vault.initialize();

        // check to make sure Vault is installed
        if (!vault.isEnabled()) {
            getLogger().info("Disabled v" + pdfFile.getVersion());
            return;
        }

        // attach to the "assassin" command
        PluginDispatcher dispatcher = new PluginDispatcher(this);
        getCommand("assassin").setExecutor(dispatcher);

        // attach the events to our listeners
        getServer().getPluginManager().registerEvents(new AutoContractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        // create the command timer instance
        timerManager = new TimerManager(this, settingsConfig.isLoggingDebug());

        // start up the metrics engine
        try {

            metrics = new Metrics(this);
            metrics.start();

        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error while enabling Metrics.");
        }

        // Okay, we're done
        getLogger().info("Enabled v" + pdfFile.getVersion());
    }

    public void onDisable() {

        PluginDescriptionFile pdfFile = getDescription();
        getLogger().info("Disabled v" + pdfFile.getVersion());

        // clear out all the static references to avoid leaks
        instance = null;
    }

    public static File getFolder() {
        return instance.getDataFolder();
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    public static VaultIntegration getVault() {
        return instance.vault;
    }

    public static PluginStorage getStorage() {
        return instance.storage;
    }

    public static SettingsConfig getSettings() {
        return instance.settingsConfig;
    }

    public static ResourcesConfig getResources() {
        return instance.resourcesConfig;
    }

    public static TimerManager getTimerManager() {
        return instance.timerManager;
    }
}
