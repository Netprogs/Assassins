package com.netprogs.minecraft.plugins.assassins.listener;

import com.netprogs.minecraft.plugins.assassins.AssassinsPlugin;
import com.netprogs.minecraft.plugins.assassins.command.util.MessageUtil;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

public class PlayerDamageListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        // if this event has already been cancelled, let's get out of here
        if (event.isCancelled()) {
            return;
        }

        // check early to see what's being damaged
        if (event.getEntityType() != EntityType.PLAYER && !(event.getEntity() instanceof Tameable)) {

            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                AssassinsPlugin.logger().info("PlayerDamage_Entity: not using target");
            }

            return;
        }

        // check early to see what's doing the damage
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Projectile)
                && !(event.getDamager() instanceof Tameable)) {

            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                AssassinsPlugin.logger().info("PlayerDamage_Entity: not using damager");
            }

            return;
        }

        // Let's find out what's getting hit first.
        Player target = null;

        // checking to see if it's a player
        if (event.getEntityType() == EntityType.PLAYER) {

            // looks like the player is being hit
            target = (Player) event.getEntity();
        }

        // still NULL, so check to see if it's a Tameable creature
        if (target == null && event.getEntity() instanceof Tameable) {

            // get the creature
            Tameable tameable = (Tameable) event.getEntity();

            // check to see if the owner of this creature is a player
            if (tameable.getOwner() != null && tameable.getOwner() instanceof Player) {

                // it is, so the target must be their pet
                target = (Player) tameable.getOwner();
            }
        }

        // no valid target, give up
        if (target == null) {
            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                AssassinsPlugin.logger().info("PlayerDamage_Entity: non-usable target");
            }
            return;
        }

        // Now we want to find out who/what is doing the damage
        Tameable tameable = null;
        Player damager = null;

        // let's see if it's a projectile
        if (event.getDamager() instanceof Projectile) {

            // let's see if a player used it
            Projectile p = (Projectile) event.getDamager();
            if (p.getShooter() instanceof Player) {
                damager = (Player) p.getShooter();
            }
        }

        // no luck yet, so check to see if the damage is being caused by a tameable creature
        if (damager == null && event.getDamager() instanceof Tameable) {

            tameable = (Tameable) event.getDamager();
            if (tameable.getOwner() != null && tameable.getOwner() instanceof Player) {
                damager = (Player) tameable.getOwner();
            }
        }

        // final check, let's see if it's another player
        if (damager == null && event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        }

        // no valid damager, give up
        if (damager == null) {
            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                AssassinsPlugin.logger().info("PlayerDamage_Entity: non-usable damager");
            }
            return;
        }

        // At this point, we have both the target and the damager as player instances.

        // Check to see if this player is currently being protected and stop him from being attacked
        if (AssassinsPlugin.getStorage().isProtectedPlayer(target.getName())) {

            if (AssassinsPlugin.getSettings().isLoggingDebug()) {
                AssassinsPlugin.logger().info("PlayerDamage_Entity: target under protection.");
            }

            // kill the damage
            event.setCancelled(true);

            // If the original damager was a creature, tell it to stop
            if (tameable != null && tameable instanceof Creature) {
                Creature creature = (Creature) tameable;
                if (creature.getTarget().equals(target)) {
                    creature.setTarget(null);
                }
            }

            // tell them they can't attack the person
            MessageUtil.sendMessage(damager, "assassins.protected.attacker", ChatColor.RED);

        } else if (AssassinsPlugin.getStorage().isProtectedPlayer(damager.getName())) {

            // Make sure the protected player can't damage back anyone while under protection
            event.setCancelled(true);

            // If the original damager was a creature, tell it to stop
            if (tameable != null && tameable instanceof Creature) {
                Creature creature = (Creature) tameable;
                if (creature.getTarget().equals(target)) {
                    creature.setTarget(null);
                }
            }

            // tell them they can't attack the person
            MessageUtil.sendMessage(damager, "assassins.protected.damager", ChatColor.RED);
        }
    }
}
