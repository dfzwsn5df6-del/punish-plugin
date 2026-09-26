package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {
    private final PunishPlugin plugin;

    public FreezeCommand(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cVerwendung: /frezz <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht online.");
            return true;
        }

        boolean frozen = !plugin.getDataManager().isFrozen(target.getUniqueId());
        plugin.getDataManager().setFrozen(target.getUniqueId(), frozen);

        if (frozen) {
            target.sendMessage("§cDu wurdest eingefroren bitte warte auf Anweisungen des Teams");
            sender.sendMessage("§aSpieler " + target.getName() + " wurde eingefroren.");
        } else {
            target.sendMessage("§aDu wurdest entfroren.");
            sender.sendMessage("§aSpieler " + target.getName() + " wurde entfroren.");
        }

        return true;
    }
}
