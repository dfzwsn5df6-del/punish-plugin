package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand implements CommandExecutor {
    private final PunishPlugin plugin;

    public PunishCommand(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cVerwendung: /punish <Spieler> <Grund>");
            sender.sendMessage("§7Erlaubte Gründe:");
            sender.sendMessage("§7- AUSZEIT");
            sender.sendMessage("§7- HAUSVERBOT");
            sender.sendMessage("§7- CHEATING");
            sender.sendMessage("§7- HACKING");
            sender.sendMessage("§7- SICHERHEITSBAN");
            sender.sendMessage("§7- CHAT_VERHALTEN");
            sender.sendMessage("§7- CHAT_BAN");
            sender.sendMessage("§7- VERWARNUNG");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht online.");
            return true;
        }

        PunishmentType type = PunishmentType.fromString(args[1]);
        if (type == null) {
            sender.sendMessage("§cUnbekannter Grund.");
            return true;
        }

        if (type == PunishmentType.SICHERHEITSBAN && !(sender.hasPermission("punish.security") || sender.hasPermission("punish.dev"))) {
            sender.sendMessage("§cDer Sicherheitsban darf nur von Admins oder Devs verwendet werden.");
            return true;
        }

        String reason = joinArgs(args, 2);

        String ip = target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "0.0.0.0";
        plugin.getDataManager().addPunishment(target.getUniqueId(), target.getName(), ip, sender.getName(), type, reason);

        if (type == PunishmentType.CHAT_BAN) {
            target.sendMessage("§cDein Chat wurde gesperrt.");
            sender.sendMessage("§aChat-Ban für " + target.getName() + " gesetzt.");
            return true;
        }

        if (type == PunishmentType.CHAT_VERHALTEN) {
            sender.sendMessage("§aVerhaltenspunkt für " + target.getName() + " gesetzt.");
            return true;
        }

        if (type == PunishmentType.VERWARNUNG) {
            sender.sendMessage("§aVerwarnung für " + target.getName() + " markiert.");
            return true;
        }

        if (type == PunishmentType.SICHERHEITSBAN) {
            target.kickPlayer("§cDu wurdest gesperrt.\n§7Grund: Sicherheitsban");
            sender.sendMessage("§aSicherheitsban für " + target.getName() + " gesetzt.");
            return true;
        }

        target.kickPlayer("§cDu wurdest bestraft.\n§7Grund: " + type.getDisplayName() + " | " + reason);
        sender.sendMessage("§aBestrafung für " + target.getName() + " gesetzt.");
        return true;
    }

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
