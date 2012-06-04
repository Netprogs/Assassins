package com.netprogs.minecraft.plugins.assassins.config;

import java.util.HashMap;
import java.util.Map;

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
 * Singleton implementation to allow the storing of any {@link Configuration} subclass. This allows you to obtain any of
 * your configurations from any parts of your code without having to pass around the instance itself.
 */
public class PluginConfig {

    private final Map<Class<?>, Configuration<?>> configurations;

    private static final PluginConfig SINGLETON = new PluginConfig();

    public static PluginConfig getInstance() {
        return SINGLETON;
    }

    private PluginConfig() {

        configurations = new HashMap<Class<?>, Configuration<?>>();
    }

    public void reset() {
        configurations.clear();
    }

    /**
     * Add a configuration instance to the manager.
     * @param configuration The configuration instance to add.
     */
    public void register(Configuration<?> configuration) {

        configuration.loadConfig();
        configurations.put(configuration.getClass(), configuration);
    }

    /**
     * Request a particular configuration instance back.
     * @param configClass The class of the configuration you'd like to request.
     * @return The configuration instance. NULL if not found.
     */
    public <T> T getConfig(Class<T> configClass) {
        return configClass.cast(configurations.get(configClass));
    }
}
