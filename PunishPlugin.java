package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PunishPlugin extends JavaPlugin implements Listener {
    private DataManager dataManager;

    @Override
    public void onEnable() {
        this.dataManager = new DataManager(this);
        dataManager.setup();

        saveDefaultConfig();

        getCommand("punish").setExecutor(new PunishCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("lookup").setExecutor(new LookupCommand(this));
        getCommand("frezz").setExecutor(new FreezeCommand(this));

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);

        getLogger().info("PunishPlugin aktiviert.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PunishPlugin deaktiviert.");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        dataManager.saveIpHistory(player);

        if (dataManager.hasActiveBan(player.getUniqueId())) {
            String reason = dataManager.getActiveBanReason(player.getUniqueId());

            player.kickPlayer("§cDu bist gebannt.\n§7Grund: " + reason);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (dataManager.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cDu wurdest eingefroren bitte warte auf Anweisungen des Teams");
        }
    }
}
