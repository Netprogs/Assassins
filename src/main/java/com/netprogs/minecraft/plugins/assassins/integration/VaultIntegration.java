package com.netprogs.minecraft.plugins.assassins.integration;

import java.util.logging.Level;

import com.netprogs.minecraft.plugins.assassins.command.ICommandType;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

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

public class VaultIntegration extends PluginIntegration {

    private boolean isPluginLoaded = false;

    private Economy economy = null;
    private Permission permission = null;

    public VaultIntegration(Plugin plugin, String basePermissionPath, boolean isLoggingDebug) {
        super(plugin, basePermissionPath, isLoggingDebug);
    }

    @Override
    public void initialize() {

        isPluginLoaded = false;

        // first we need to check to see if Vault is actually installed
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            getPlugin().getLogger().log(Level.SEVERE, "Vault is not installed.");
            return;
        }

        // try to obtain the economy class from Vault
        RegisteredServiceProvider<Economy> economyProvider =
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null) {

            economy = (Economy) economyProvider.getProvider();

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("Vault:Economy integration successful.");
            }

        } else {

            getPlugin().getLogger().log(Level.SEVERE, "Could not obtain an Economy integration from Vault.");
            return;
        }

        // try to obtain the permission class from Vault
        RegisteredServiceProvider<Permission> permissionProvider =
                Bukkit.getServer().getServicesManager().getRegistration(Permission.class);

        if (permissionProvider != null) {

            permission = (Permission) permissionProvider.getProvider();

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("Vault:Permission integration successful.");
            }

        } else {

            getPlugin().getLogger().log(Level.SEVERE, "Could not obtain a Permission integration from Vault.");
            return;
        }
        // set the isPluginLoaded flag
        isPluginLoaded = true;

        return;
    }

    @Override
    protected boolean isPluginLoaded() {
        return isPluginLoaded;
    }

    @Override
    protected boolean isPluginEnabled() {
        // we have to have this, so don't allow config to turn it off
        return true;
    }

    /**
     * Since this is a required integration, we can safely expose the Economy instance within it.
     * @return
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Since this is a required integration, we can safely expose the Permission instance within it.
     * @return
     */
    public Permission getPermission() {
        return permission;
    }

    public boolean hasCommandPermission(CommandSender sender, ICommandType commandType) {
        return hasCommandPermission(sender, commandType.toString());
    }

    public boolean hasCommandPermission(CommandSender sender, String permissionPath) {

        String path = getBasePermissionPath() + "." + permissionPath;

        boolean hasPermission = permission.has(sender, path);
        if (hasPermission) {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info(sender.getName() + " has the permission: " + path);
            }

        } else {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info(sender.getName() + " does not have the permission: " + path);
            }
        }

        return hasPermission;
    }

    public boolean withdrawContractPayment(String playerName, double payment) {

        // now check to see if they have enough money
        if (economy.has(playerName, payment)) {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("[withdraw] Charging: " + payment);
            }

            // do the actual withdraw now
            EconomyResponse response = economy.withdrawPlayer(playerName, payment);
            if (response.transactionSuccess()) {

                if (isLoggingDebug()) {
                    getPlugin().getLogger().info("[withdraw]: " + payment);
                }

            } else {

                if (isLoggingDebug()) {
                    getPlugin().getLogger().info("[withdraw] failed: " + response.errorMessage);
                }

                // They seem to have run out of money.
                return false;
            }

        } else {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("[withdraw] Not enough funds for payment: " + payment);
            }

            // They seem to have run out of money.
            return false;
        }

        return true;
    }

    public boolean depositContractPayment(String playerName, double payment) {

        // put the payment onto the players account
        EconomyResponse response = economy.depositPlayer(playerName, payment);
        if (response.transactionSuccess()) {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("[deposit]: " + payment);
            }

            return true;

        } else {

            if (isLoggingDebug()) {
                getPlugin().getLogger().info("[deposit] failed: " + response.errorMessage);
            }

            return false;
        }
    }
}
