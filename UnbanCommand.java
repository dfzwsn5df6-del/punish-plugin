package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCommand implements CommandExecutor {
    private final PunishPlugin plugin;

    public UnbanCommand(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cVerwendung: /unban <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target != null) {
            target.sendMessage("§aDein Bann wurde aufgehoben.");
        }

        plugin.getDataManager().unbanPlayerByName(args[0]);
        sender.sendMessage("§aBestrafung für " + args[0] + " aufgehoben.");
        return true;
    }
}
