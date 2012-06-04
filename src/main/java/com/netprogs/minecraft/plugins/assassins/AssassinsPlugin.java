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
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.command.PluginDispatcher;
import com.netprogs.minecraft.plugins.assassins.config.PluginConfig;
import com.netprogs.minecraft.plugins.assassins.config.resources.ResourcesConfig;
import com.netprogs.minecraft.plugins.assassins.config.settings.SettingsConfig;
import com.netprogs.minecraft.plugins.assassins.integration.VaultIntegration;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerDamageListener;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerDeathListener;
import com.netprogs.minecraft.plugins.assassins.listener.PlayerQuitListener;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AssassinsPlugin extends JavaPlugin {

    private final Logger logger = Logger.getLogger("Minecraft");

    // expose the instance of this class as a global so we can better access it's methods
    public static AssassinsPlugin instance;

    private String pluginName;
    private File pluginFolder;

    public AssassinsPlugin() {
        instance = this;
    }

    public void onEnable() {

        // report that this plug in is being loaded
        PluginDescriptionFile pdfFile = getDescription();

        pluginName = getDescription().getName();
        pluginFolder = getDataFolder();

        // load the rank data from the XML file
        loadConfigurations();

        // check to make sure Vault is installed
        VaultIntegration.getInstance().initialize(this);
        if (!VaultIntegration.getInstance().isEnabled()) {
            logger.info("[" + pdfFile.getName() + "] v" + pdfFile.getVersion() + " has been disabled.");
            return;
        }

        // attach to the "assassin" command
        PluginDispatcher dispatcher = new PluginDispatcher(this);
        getCommand("assassin").setExecutor(dispatcher);

        // attach the events to our listeners
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        // Okay, we're done
        logger.info("[" + pdfFile.getName() + "] v" + pdfFile.getVersion() + " has been enabled.");
    }

    public void loadConfigurations() {

        PluginConfig.getInstance().reset();
        PluginConfig.getInstance().register(new SettingsConfig(getDataFolder() + "/config.json"));
        PluginConfig.getInstance().register(new ResourcesConfig(getDataFolder() + "/resources.json"));
    }

    public void onDisable() {

        PluginDescriptionFile pdfFile = getDescription();
        this.logger.info("[" + pdfFile.getName() + "] has been disabled.");
    }

    public String getPluginName() {
        return pluginName;
    }

    public File getPluginFolder() {
        return pluginFolder;
    }
}
