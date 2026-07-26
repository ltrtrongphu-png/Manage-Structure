package com.example.managestructure;

import org.bukkit.Location;

public class Region {

    private final String name;
    private final String world;
    private final int minX, minY, minZ, maxX, maxY, maxZ;
    private final String permission;

    public Region(String name, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.name = name;
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.permission = "managestructure.access." + name.toLowerCase();
    }

    public boolean contains(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getPermission() {
        return permission;
    }

    public double centerX() {
        return (minX + maxX) / 2.0 + 0.5;
    }

    public double centerY() {
        return (minY + maxY) / 2.0 + 0.5;
    }

    public double centerZ() {
        return (minZ + maxZ) / 2.0 + 0.5;
    }

    public long volume() {
        long sx = (long) (maxX - minX + 1);
        long sy = (long) (maxY - minY + 1);
        long sz = (long) (maxZ - minZ + 1);
        return sx * sy * sz;
    }
}
