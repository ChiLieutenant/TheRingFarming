package com.chilieutenant.theringfarming;

import com.chilieutenant.theringfarming.handlers.Farm;
import com.chilieutenant.theringfarming.handlers.FarmCommand;
import com.chilieutenant.theringfarming.handlers.FarmTabCompleter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Main instance;
    public static Economy econ = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        if (!setupEconomy() ) {
            Logger.getLogger("Minecraft").severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Farm.manageFarms();
        Farm.loadFarms();
        this.getCommand("farm").setExecutor(new FarmCommand());
        this.getCommand("farm").setTabCompleter(new FarmTabCompleter());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
