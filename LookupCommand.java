package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LookupCommand implements CommandExecutor {
    private final PunishPlugin plugin;

    public LookupCommand(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cVerwendung: /lookup <all|alt> <Spieler>");
            return true;
        }

        String mode = args[0].toLowerCase();
        String playerName = args[1];

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("§7Spieler offline, suche trotzdem nach Daten.");
        }

        if ("all".equals(mode)) {
            Player p = Bukkit.getPlayerExact(playerName);
            if (p == null) {
                sender.sendMessage("§cOffline-Lookup nur über Namen möglich.");
                return true;
            }

            List<String> punishments = plugin.getDataManager().getAllPunishmentLines(p.getUniqueId());

            sender.sendMessage("§6=== Lookup all: " + p.getName() + " ===");
            for (String line : punishments) {
                sender.sendMessage(line);
            }
            return true;
        }

        if ("alt".equals(mode)) {
            String ip = "0.0.0.0";
            Player p = Bukkit.getPlayerExact(playerName);
            if (p != null) {
                ip = p.getAddress() != null ? p.getAddress().getAddress().getHostAddress() : ip;
            }

            List<String> alts = plugin.getDataManager().findAltsByIp(ip);

            sender.sendMessage("§6=== Lookup alt: " + playerName + " ===");
            if (alts.isEmpty()) {
                sender.sendMessage("§7Keine alten Accounts mit derselben IP gefunden.");
                return true;
            }

            for (String alt : alts) {
                sender.sendMessage("§7- " + alt);
            }
            return true;
        }

        sender.sendMessage("§cUnbekannter Lookup-Modus.");
        return true;
    }
}
