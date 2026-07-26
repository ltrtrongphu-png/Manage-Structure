package com.example.managestructure;

import org.bukkit.plugin.java.JavaPlugin;

public class ManageStructure extends JavaPlugin {

    private static ManageStructure instance;
    private RegionManager regionManager;
    private SelectionManager selectionManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.regionManager = new RegionManager(this);
        this.selectionManager = new SelectionManager();

        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        StructureCommand executor = new StructureCommand(this);
        getCommand("managestructure").setExecutor(executor);
        getCommand("managestructure").setTabCompleter(executor);

        getLogger().info("ManageStructure da duoc kich hoat! Dang bao ve " + regionManager.getRegions().size() + " vung cong trinh.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ManageStructure da tat.");
    }

    public static ManageStructure getInstance() {
        return instance;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public String msg(String path) {
        String m = getConfig().getString("messages." + path, "");
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', m);
    }

    public boolean setting(String path) {
        return getConfig().getBoolean("settings." + path, true);
    }
}
