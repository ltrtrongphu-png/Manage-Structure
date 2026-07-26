package com.example.managestructure;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public void setPos1(UUID uuid, Location loc) {
        pos1.put(uuid, loc);
    }

    public void setPos2(UUID uuid, Location loc) {
        pos2.put(uuid, loc);
    }

    public Location getPos1(UUID uuid) {
        return pos1.get(uuid);
    }

    public Location getPos2(UUID uuid) {
        return pos2.get(uuid);
    }

    public void clear(UUID uuid) {
        pos1.remove(uuid);
        pos2.remove(uuid);
    }

    public boolean hasBoth(UUID uuid) {
        return pos1.containsKey(uuid) && pos2.containsKey(uuid);
    }
}
