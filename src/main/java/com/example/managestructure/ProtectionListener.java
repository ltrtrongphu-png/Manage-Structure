package com.example.managestructure;

import org.bukkit.Location;
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
import org.bukkit.loot.Lootable;

public class ProtectionListener implements Listener {

    private final ManageStructure plugin;

    public ProtectionListener(ManageStructure plugin) {
        this.plugin = plugin;
    }

    // ----- Quyen chung cho auto-detect (khong can vung thu cong) -----
    private boolean canBypassAuto(Player player) {
        return player.hasPermission("managestructure.bypass") || player.hasPermission("managestructure.access");
    }

    // ----- Quyen rieng cho tung vung thu cong -----
    private boolean canBypassRegion(Player player, Region region) {
        return player.hasPermission("managestructure.bypass") || player.hasPermission(region.getPermission());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        // 1) Kiem tra vung thu cong truoc
        if (plugin.setting("protect-manual-regions")) {
            Region region = plugin.getRegionManager().getRegionAt(loc);
            if (region != null) {
                if (canBypassRegion(player, region)) return;
                event.setCancelled(true);
                player.sendMessage(plugin.msg("no-permission-break").replace("%block%", region.getName()));
                return;
            }
        }

        // 2) Auto-detect theo loai block dac trung
        if (plugin.setting("auto-detect-blocks") && plugin.isAutoProtectedBlock(event.getBlock().getType())) {
            if (canBypassAuto(player)) return;
            event.setCancelled(true);
            player.sendMessage(plugin.msg("no-permission-break").replace("%block%", event.getBlock().getType().name()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Chi chan dat block trong VUNG THU CONG, khong ap dung cho auto-detect
        // (nguoi choi xay nha bang nether brick/blackstone/purpur binh thuong khong nen bi chan)
        if (!plugin.setting("protect-manual-regions") || !plugin.setting("protect-place")) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());
        if (region == null) return;

        Player player = event.getPlayer();
        if (canBypassRegion(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-place").replace("%region%", region.getName()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        Location loc = event.getInventory().getLocation();

        // 1) Vung thu cong: chan mo bat ky do chua nao trong vung
        if (loc != null && plugin.setting("protect-manual-regions")) {
            Region region = plugin.getRegionManager().getRegionAt(loc);
            if (region != null) {
                if (canBypassRegion(player, region)) return;
                event.setCancelled(true);
                player.sendMessage(plugin.msg("no-permission-container").replace("%region%", region.getName()));
                return;
            }
        }

        // 2) Auto-detect: do chua CHUA TUNG DUOC MO (con loot table) o BAT KY DAU
        // trong the gioi -> coi la ruong cong trinh (fortress/bastion/end city/ruined portal...)
        if (plugin.setting("auto-detect-containers") && holder instanceof Lootable lootable) {
            if (lootable.getLootTable() != null) {
                if (canBypassAuto(player)) return;
                event.setCancelled(true);
                player.sendMessage(plugin.msg("no-permission-container").replace("%region%", "?"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.setting("protect-explosions")) return;
        event.blockList().removeIf(this::isProtectedByAnyMeans);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.setting("protect-explosions")) return;
        event.blockList().removeIf(this::isProtectedByAnyMeans);
    }

    private boolean isProtectedByAnyMeans(org.bukkit.block.Block block) {
        if (plugin.setting("protect-manual-regions") && plugin.getRegionManager().getRegionAt(block.getLocation()) != null) {
            return true;
        }
        return plugin.setting("auto-detect-blocks") && plugin.isAutoProtectedBlock(block.getType());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        // Item frame / tranh treo / armor stand chi duoc bao ve trong VUNG THU CONG
        // (khong co "material" de auto-detect entity kieu nay o ngoai vung)
        if (!plugin.setting("protect-hanging") || !plugin.setting("protect-manual-regions")) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getEntity().getLocation());
        if (region == null) return;

        if (event instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof Player player) {
                if (canBypassRegion(player, region)) return;
                event.setCancelled(true);
                player.sendMessage(plugin.msg("no-permission-hanging").replace("%region%", region.getName()));
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!plugin.setting("protect-hanging") || !plugin.setting("protect-manual-regions")) return;
        if (event.getEntityType() != EntityType.ARMOR_STAND) return;
        if (!(event.getDamager() instanceof Player player)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getEntity().getLocation());
        if (region == null) return;

        if (canBypassRegion(player, region)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.msg("no-permission-hanging").replace("%region%", region.getName()));
    }
}
