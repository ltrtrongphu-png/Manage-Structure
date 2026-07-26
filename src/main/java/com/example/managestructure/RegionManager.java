package com.example.managestructure;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegionManager {

    private final ManageStructure plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final Map<String, Region> regions = new LinkedHashMap<>();

    public RegionManager(ManageStructure plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "regions.yml");
        load();
    }

    public void load() {
        regions.clear();

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Khong the tao file regions.yml: " + e.getMessage());
            }
        }

        yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("regions");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection r = section.getConfigurationSection(key);
            if (r == null) continue;
            String world = r.getString("world");
            int x1 = r.getInt("x1");
            int y1 = r.getInt("y1");
            int z1 = r.getInt("z1");
            int x2 = r.getInt("x2");
            int y2 = r.getInt("y2");
            int z2 = r.getInt("z2");
            if (world == null) continue;
            Region region = new Region(key, world, x1, y1, z1, x2, y2, z2);
            regions.put(key.toLowerCase(), region);
        }
    }

    public void saveToDisk() {
        yaml.set("regions", null);
        for (Region r : regions.values()) {
            String path = "regions." + r.getName();
            yaml.set(path + ".world", r.getWorld());
            yaml.set(path + ".x1", r.getMinX());
            yaml.set(path + ".y1", r.getMinY());
            yaml.set(path + ".z1", r.getMinZ());
            yaml.set(path + ".x2", r.getMaxX());
            yaml.set(path + ".y2", r.getMaxY());
            yaml.set(path + ".z2", r.getMaxZ());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Khong the luu regions.yml: " + e.getMessage());
        }
    }

    public boolean exists(String name) {
        return regions.containsKey(name.toLowerCase());
    }

    public Region createRegion(String name, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        Region region = new Region(name, world, x1, y1, z1, x2, y2, z2);
        regions.put(name.toLowerCase(), region);
        saveToDisk();
        return region;
    }

    public boolean removeRegion(String name) {
        Region removed = regions.remove(name.toLowerCase());
        if (removed != null) {
            saveToDisk();
            return true;
        }
        return false;
    }

    public Region getRegion(String name) {
        return regions.get(name.toLowerCase());
    }

    public Collection<Region> getRegions() {
        return regions.values();
    }

    public List<String> getRegionNames() {
        List<String> names = new ArrayList<>();
        for (Region r : regions.values()) {
            names.add(r.getName());
        }
        return names;
    }

    /** Tim vung dau tien chua vi tri nay (neu co nhieu vung chong nhau se lay vung dau tien tim thay) */
    public Region getRegionAt(org.bukkit.Location loc) {
        for (Region r : regions.values()) {
            if (r.contains(loc)) {
                return r;
            }
        }
        return null;
    }
}
