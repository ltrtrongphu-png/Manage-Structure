package com.example.managestructure;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class ProtectionListener implements Listener {

    private final ManageStructure plugin;

    public ProtectionListener(ManageStructure plugin) {
        this.plugin = plugin;
    }

    private boolean canBypass(Player player, Region region) {
        return player.hasPermission("managestructure.bypass") || player.hasPermission(region.getPermission());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.setting("protect-break")) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());
        if (region == null) return;

        Player player = event.getPlayer();
        if (canBypass(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-break").replace("%region%", region.getName()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.setting("protect-place")) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());
        if (region == null) return;

        Player player = event.getPlayer();
        if (canBypass(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-place").replace("%region%", region.getName()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.setting("protect-containers")) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        org.bukkit.Location loc = event.getInventory().getLocation();
        if (loc == null) return;

        Region region = plugin.getRegionManager().getRegionAt(loc);
        if (region == null) return;

        if (canBypass(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-container").replace("%region%", region.getName()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.setting("protect-explosions")) return;
        event.blockList().removeIf(b -> plugin.getRegionManager().getRegionAt(b.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.setting("protect-explosions")) return;
        event.blockList().removeIf(b -> plugin.getRegionManager().getRegionAt(b.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!plugin.setting("protect-hanging")) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getEntity().getLocation());
        if (region == null) return;

        if (event instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof Player player) {
                if (canBypass(player, region)) return;
                event.setCancelled(true);
                player.sendMessage(plugin.msg("no-permission-hanging").replace("%region%", region.getName()));
                return;
            }
        }

        // Nguyen nhan khac (no, vat ly...) trong vung bao ve -> luon chan de tranh grief
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!plugin.setting("protect-hanging")) return;
        if (event.getEntityType() != EntityType.ARMOR_STAND) return;
        if (!(event.getDamager() instanceof Player player)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getEntity().getLocation());
        if (region == null) return;

        if (canBypass(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-hanging").replace("%region%", region.getName()));
    }
}
