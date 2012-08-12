package com.netprogs.minecraft.plugins.assassins.command.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.netprogs.minecraft.plugins.assassins.command.ICommandType;

import org.bukkit.plugin.Plugin;

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

/**
 * This utility class keeps track of timers for commands and events.
 * The information stored here is not persisted, so when the server is restarted, the timers are cleared.
 */
public class TimerManager {

    private Logger logger;
    private boolean isLoggingDebug;

    // Map<PlayerName, Map<CommandType, TimeInSeconds>>
    private final Map<String, Map<ICommandType, Long>> commandTimers = new HashMap<String, Map<ICommandType, Long>>();

    // Map<PlayerName, Map<StringEventName, TimeInSeconds>>
    private final Map<String, Map<String, Long>> eventTimers = new HashMap<String, Map<String, Long>>();

    private final ReentrantReadWriteLock rwCommandLock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock rwEventLock = new ReentrantReadWriteLock(true);

    public TimerManager(Plugin plugin, boolean isLoggingDebug) {

        logger = plugin.getLogger();
        this.isLoggingDebug = isLoggingDebug;
    }

    /**
     * Cleans up old timers to let them get GC and reduce memory stamp.
     */
    private void cleanCommandTimers() {

        for (Iterator<String> it = commandTimers.keySet().iterator(); it.hasNext();) {

            String playerName = it.next();
            Map<ICommandType, Long> playerMap = commandTimers.get(playerName);

            // get the timer map for the player
            for (Iterator<ICommandType> playerMapIterator = playerMap.keySet().iterator(); playerMapIterator.hasNext();) {

                ICommandType commandType = playerMapIterator.next();
                long lastCommandTime = playerMap.get(commandType);

                if (lastCommandTime <= System.currentTimeMillis()) {
                    if (isLoggingDebug) {
                        logger.info("Removing expired command timer: [" + playerName + ", " + commandType + "]");
                    }
                    playerMapIterator.remove();
                }
            }
        }
    }

    /**
     * Cleans up old timers to let them get GC and reduce memory stamp.
     */
    private void cleanEventTimers() {

        for (Iterator<String> it = eventTimers.keySet().iterator(); it.hasNext();) {

            String playerName = it.next();
            Map<String, Long> playerMap = eventTimers.get(playerName);

            // get the timer map for the player
            for (Iterator<String> playerMapIterator = playerMap.keySet().iterator(); playerMapIterator.hasNext();) {

                String eventType = playerMapIterator.next();
                long lastCommandTime = playerMap.get(eventType);

                if (lastCommandTime <= System.currentTimeMillis()) {
                    logger.info("Removing expired event timer: [" + playerName + ", " + eventType + "]");
                    playerMapIterator.remove();
                }
            }
        }
    }

    /**
     * Determines if the provided command is on timer for the user.
     * @param player The name of the player running the command.
     * @param socialCommand The command being run.
     * @return Amount of time remaining. If 0, means it's not on timer.
     */
    public long commandOnTimer(String playerName, ICommandType commandType) {

        Lock lock = rwCommandLock.readLock();
        lock.lock();
        try {

            // check the timer map to see if they have one there already
            Map<ICommandType, Long> timerInfo = commandTimers.get(playerName);
            if (timerInfo != null && timerInfo.containsKey(commandType)) {

                long lastCommandTime = timerInfo.get(commandType);

                if (isLoggingDebug) {
                    logger.info("commandOnTimer, lastCommandTime: " + formatTimeShort(lastCommandTime) + " > "
                            + formatTimeShort(System.currentTimeMillis()));
                }

                // check to see if they're allowed to use the command
                if (lastCommandTime > System.currentTimeMillis()) {

                    long remaining = (lastCommandTime - System.currentTimeMillis());

                    if (isLoggingDebug) {
                        logger.info("commandOnTimer, timeRemaining: " + formatTimeShort(remaining));
                    }

                    // It's on timer, return the time
                    return remaining;
                }
            }

            return 0L;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates the command timer for the player.
     * @param playerName The name of the player running the command.
     * @param commandType The command type.
     * @param timer The new timer period to assign (in seconds).
     */
    public void updateCommandTimer(String playerName, ICommandType commandType, long timer) {

        Lock lock = rwCommandLock.writeLock();
        lock.lock();
        try {

            // clean out old timers
            cleanCommandTimers();

            if (isLoggingDebug) {
                logger.info("updateCommandTimer: " + commandType + " " + timer);
            }

            // check the timer map to see if they have one there already
            Map<ICommandType, Long> timerInfo = commandTimers.get(playerName);
            if (timerInfo == null) {

                // nope, let's create it
                timerInfo = new HashMap<ICommandType, Long>();
                commandTimers.put(playerName, timerInfo);

                if (isLoggingDebug) {
                    logger.info("Created new timer entry for: " + commandType);
                }
            }

            if (isLoggingDebug) {
                logger.info("Updating timer for command: " + commandType + " to: "
                        + formatTimeShort(System.currentTimeMillis() + (timer * 1000)));
            }

            // now update the cooldown
            timerInfo.put(commandType, (System.currentTimeMillis() + (timer * 1000)));

        } finally {
            lock.unlock();
        }
    }

    /**
     * Determines if the provided command is on timer for the user.
     * @param player The name of the player running the command.
     * @param socialCommand The command being run.
     * @return Amount of time remaining. If 0, means it's not on timer.
     */
    public long eventOnTimer(String playerName, String eventType) {

        Lock lock = rwEventLock.readLock();
        lock.lock();
        try {

            // check the timer map to see if they have one there already
            Map<String, Long> timerInfo = eventTimers.get(playerName);
            if (timerInfo != null && timerInfo.containsKey(eventType)) {

                long lastCommandTime = timerInfo.get(eventType);

                if (isLoggingDebug) {
                    logger.info("eventOnTimer, lastCommandTime: " + formatTimeShort(lastCommandTime) + " > "
                            + formatTimeShort(System.currentTimeMillis()));
                }

                // check to see if they're allowed to use the command
                if (lastCommandTime > System.currentTimeMillis()) {

                    long remaining = (lastCommandTime - System.currentTimeMillis());

                    if (isLoggingDebug) {
                        logger.info("eventOnTimer, timeRemaining: " + formatTimeShort(remaining));
                    }

                    // It's on timer, return the time
                    return remaining;
                }
            }

            return 0L;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates the command timer for the player.
     * @param playerName The name of the player running the command.
     * @param eventType The event name.
     * @param timer The new timer period to assign (in seconds).
     */
    public void updateEventTimer(String playerName, String eventType, long timer) {

        Lock lock = rwEventLock.writeLock();
        lock.lock();
        try {

            // clean out old timers
            cleanEventTimers();

            if (isLoggingDebug) {
                logger.info("updateEventTimer: " + eventType + " " + timer);
            }

            // check the timer map to see if they have one there already
            Map<String, Long> timerInfo = eventTimers.get(playerName);
            if (timerInfo == null) {

                // nope, let's create it
                timerInfo = new HashMap<String, Long>();
                eventTimers.put(playerName, timerInfo);

                if (isLoggingDebug) {
                    logger.info("Created new timer entry for: " + eventType);
                }
            }

            if (isLoggingDebug) {
                logger.info("Updating timer for event: " + eventType + " to: "
                        + formatTimeShort(System.currentTimeMillis() + (timer * 1000)));
            }

            // now update the cooldown
            timerInfo.put(eventType, (System.currentTimeMillis() + (timer * 1000)));

        } finally {
            lock.unlock();
        }
    }

    public static String formatTimeShort(long time) {

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat minFormat = new SimpleDateFormat("mm");
        minFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat secFormat = new SimpleDateFormat("ss");
        secFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String hours = hourFormat.format(time);
        String minutes = minFormat.format(time);
        String seconds = secFormat.format(time);

        String timeRemaining = hours + ":" + minutes + ":" + seconds;
        return timeRemaining;
    }

    public static String formatTimeLong(long time) {

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat minFormat = new SimpleDateFormat("mm");
        minFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat secFormat = new SimpleDateFormat("ss");
        secFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String timeRemaining = hourFormat.format(time) + " hours, ";
        timeRemaining += minFormat.format(time) + " minutes, ";
        timeRemaining += secFormat.format(time) + " seconds";

        return timeRemaining;
    }
}
