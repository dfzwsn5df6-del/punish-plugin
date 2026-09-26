package de.yourserver.punish;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {
    private final PunishPlugin plugin;

    public FreezeListener(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getDataManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
