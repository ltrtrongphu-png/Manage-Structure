package com.example.managestructure;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageStructure extends JavaPlugin {

    private static ManageStructure instance;
    private RegionManager regionManager;
    private SelectionManager selectionManager;
    private final Set<Material> autoBreakBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.regionManager = new RegionManager(this);
        this.selectionManager = new SelectionManager();
        loadAutoBreakBlocks();

        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        StructureCommand executor = new StructureCommand(this);
        getCommand("managestructure").setExecutor(executor);
        getCommand("managestructure").setTabCompleter(executor);

        getLogger().info("ManageStructure da duoc kich hoat! Auto-detect " + autoBreakBlocks.size()
                + " loai block, dang bao ve " + regionManager.getRegions().size() + " vung thu cong.");
    }

    public void loadAutoBreakBlocks() {
        autoBreakBlocks.clear();
        List<String> list = getConfig().getStringList("auto-break-blocks");
        for (String s : list) {
            Material m = Material.matchMaterial(s.trim().toUpperCase());
            if (m != null) {
                autoBreakBlocks.add(m);
            } else {
                getLogger().warning("Khong tim thay Material: " + s + " trong auto-break-blocks, da bo qua.");
            }
        }
    }

    public boolean isAutoProtectedBlock(Material material) {
        return autoBreakBlocks.contains(material);
    }

    public Set<Material> getAutoBreakBlocks() {
        return autoBreakBlocks;
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
