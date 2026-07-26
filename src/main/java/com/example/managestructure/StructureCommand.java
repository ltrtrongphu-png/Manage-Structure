package com.example.managestructure;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StructureCommand implements CommandExecutor, TabCompleter {

    private final ManageStructure plugin;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "pos1", "pos2", "create", "remove", "list", "info", "tp", "locate", "reload", "help"
    );

    private static final List<String> LOCATE_TYPES = Arrays.asList(
            "fortress", "bastion_remnant", "end_city", "ruined_portal", "nether_fossil"
    );

    public StructureCommand(ManageStructure plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("managestructure.admin")) {
            sender.sendMessage(ChatColor.RED + "Ban khong co quyen dung lenh nay.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        RegionManager rm = plugin.getRegionManager();

        switch (args[0].toLowerCase()) {
            case "pos1" -> handlePos(sender, true);
            case "pos2" -> handlePos(sender, false);
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "tp" -> handleTp(sender, args);
            case "locate" -> handleLocate(sender, args);
            case "reload" -> {
                plugin.reloadConfig();
                rm.load();
                sender.sendMessage(ChatColor.GREEN + "Da reload config.yml va regions.yml!");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handlePos(CommandSender sender, boolean isPos1) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chi nguoi choi moi dung duoc lenh nay.");
            return;
        }

        Location target = getTargetOrStandingLocation(player);

        if (isPos1) {
            plugin.getSelectionManager().setPos1(player.getUniqueId(), target);
        } else {
            plugin.getSelectionManager().setPos2(player.getUniqueId(), target);
        }

        player.sendMessage(plugin.msg("pos-set")
                .replace("%pos%", isPos1 ? "pos1" : "pos2")
                .replace("%x%", String.valueOf(target.getBlockX()))
                .replace("%y%", String.valueOf(target.getBlockY()))
                .replace("%z%", String.valueOf(target.getBlockZ()))
                .replace("%world%", target.getWorld().getName()));
    }

    private Location getTargetOrStandingLocation(Player player) {
        RayTraceResult result = player.rayTraceBlocks(100);
        if (result != null && result.getHitBlock() != null) {
            Block block = result.getHitBlock();
            return block.getLocation();
        }
        return player.getLocation();
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Dung: /ms create <ten_vung>");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chi nguoi choi moi dung duoc lenh nay.");
            return;
        }

        String name = args[1];
        RegionManager rm = plugin.getRegionManager();

        if (rm.exists(name)) {
            sender.sendMessage(plugin.msg("region-exists").replace("%region%", name));
            return;
        }

        Location p1 = plugin.getSelectionManager().getPos1(player.getUniqueId());
        Location p2 = plugin.getSelectionManager().getPos2(player.getUniqueId());

        if (p1 == null || p2 == null) {
            sender.sendMessage(plugin.msg("need-both-positions"));
            return;
        }

        if (!p1.getWorld().equals(p2.getWorld())) {
            sender.sendMessage(plugin.msg("different-worlds"));
            return;
        }

        if (plugin.setting("only-nether-and-end")) {
            World.Environment env = p1.getWorld().getEnvironment();
            if (env != World.Environment.NETHER && env != World.Environment.THE_END) {
                sender.sendMessage(plugin.msg("wrong-world-type"));
                return;
            }
        }

        Region region = rm.createRegion(name, p1.getWorld().getName(),
                p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
                p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());

        plugin.getSelectionManager().clear(player.getUniqueId());

        sender.sendMessage(plugin.msg("region-created")
                .replace("%region%", region.getName())
                .replace("%permission%", region.getPermission()));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Dung: /ms remove <ten_vung>");
            return;
        }
        RegionManager rm = plugin.getRegionManager();
        if (!rm.removeRegion(args[1])) {
            sender.sendMessage(plugin.msg("region-not-found").replace("%region%", args[1]));
            return;
        }
        sender.sendMessage(plugin.msg("region-removed").replace("%region%", args[1]));
    }

    private void handleList(CommandSender sender) {
        RegionManager rm = plugin.getRegionManager();
        if (rm.getRegions().isEmpty()) {
            sender.sendMessage(plugin.msg("region-list-empty"));
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Danh sach vung bao ve (" + rm.getRegions().size() + ") ===");
        for (Region r : rm.getRegions()) {
            sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + r.getName()
                    + ChatColor.GRAY + " (" + r.getWorld() + ") quyen: "
                    + ChatColor.AQUA + r.getPermission());
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Dung: /ms info <ten_vung>");
            return;
        }
        Region r = plugin.getRegionManager().getRegion(args[1]);
        if (r == null) {
            sender.sendMessage(plugin.msg("region-not-found").replace("%region%", args[1]));
            return;
        }
        sender.sendMessage(plugin.msg("region-info").replace("%region%", r.getName()));
        sender.sendMessage(ChatColor.GRAY + "The gioi: " + ChatColor.WHITE + r.getWorld());
        sender.sendMessage(ChatColor.GRAY + "Goc 1: " + ChatColor.WHITE
                + r.getMinX() + ", " + r.getMinY() + ", " + r.getMinZ());
        sender.sendMessage(ChatColor.GRAY + "Goc 2: " + ChatColor.WHITE
                + r.getMaxX() + ", " + r.getMaxY() + ", " + r.getMaxZ());
        sender.sendMessage(ChatColor.GRAY + "The tich: " + ChatColor.WHITE + r.volume() + " block");
        sender.sendMessage(ChatColor.GRAY + "Quyen: " + ChatColor.AQUA + r.getPermission());
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chi nguoi choi moi dung duoc lenh nay.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Dung: /ms tp <ten_vung>");
            return;
        }
        Region r = plugin.getRegionManager().getRegion(args[1]);
        if (r == null) {
            sender.sendMessage(plugin.msg("region-not-found").replace("%region%", args[1]));
            return;
        }
        World world = plugin.getServer().getWorld(r.getWorld());
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "The gioi " + r.getWorld() + " khong ton tai hoac chua duoc load.");
            return;
        }
        Location loc = new Location(world, r.centerX(), r.centerY(), r.centerZ());
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Da dich chuyen den vung " + r.getName() + ".");
    }

    private void handleLocate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chi nguoi choi moi dung duoc lenh nay.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Dung: /ms locate <fortress|bastion_remnant|end_city|ruined_portal|nether_fossil> [ban_kinh]");
            return;
        }

        String key = args[1].toLowerCase();
        Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(key));
        if (structure == null) {
            sender.sendMessage(plugin.msg("locate-invalid-type").replace("%type%", key));
            return;
        }

        int radius = 100;
        if (args.length >= 3) {
            try {
                radius = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
                // giu gia tri mac dinh
            }
        }

        try {
            var result = player.getWorld().locateNearestStructure(player.getLocation(), structure, radius, false);
            if (result == null) {
                sender.sendMessage(plugin.msg("locate-not-found"));
                return;
            }
            Location loc = result.getLocation();
            sender.sendMessage(plugin.msg("locate-found")
                    .replace("%world%", loc.getWorld().getName())
                    .replace("%x%", String.valueOf(loc.getBlockX()))
                    .replace("%y%", String.valueOf(loc.getBlockY()))
                    .replace("%z%", String.valueOf(loc.getBlockZ())));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Loi khi tim cong trinh: " + e.getMessage());
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ManageStructure - Lenh quan tri ===");
        sender.sendMessage(ChatColor.AQUA + "/ms pos1 " + ChatColor.GRAY + "- Danh dau diem 1 (block dang nhin hoac dung dung)");
        sender.sendMessage(ChatColor.AQUA + "/ms pos2 " + ChatColor.GRAY + "- Danh dau diem 2");
        sender.sendMessage(ChatColor.AQUA + "/ms create <ten> " + ChatColor.GRAY + "- Tao vung bao ve tu pos1/pos2");
        sender.sendMessage(ChatColor.AQUA + "/ms remove <ten> " + ChatColor.GRAY + "- Xoa vung bao ve");
        sender.sendMessage(ChatColor.AQUA + "/ms list " + ChatColor.GRAY + "- Xem danh sach vung");
        sender.sendMessage(ChatColor.AQUA + "/ms info <ten> " + ChatColor.GRAY + "- Xem chi tiet 1 vung");
        sender.sendMessage(ChatColor.AQUA + "/ms tp <ten> " + ChatColor.GRAY + "- Dich chuyen den giua vung");
        sender.sendMessage(ChatColor.AQUA + "/ms locate <loai> [ban_kinh] " + ChatColor.GRAY + "- Tim cong trinh gan nhat (ho tro chon pos1/pos2)");
        sender.sendMessage(ChatColor.AQUA + "/ms reload " + ChatColor.GRAY + "- Tai lai config.yml va regions.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("managestructure.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("remove") || sub.equals("info") || sub.equals("tp")) {
                return filter(plugin.getRegionManager().getRegionNames(), args[1]);
            }
            if (sub.equals("locate")) {
                return filter(LOCATE_TYPES, args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String low = input.toLowerCase();
        List<String> result = new ArrayList<>(options.stream()
                .filter(s -> s.toLowerCase().startsWith(low))
                .collect(Collectors.toList()));
        Collections.sort(result);
        return result;
    }
}
