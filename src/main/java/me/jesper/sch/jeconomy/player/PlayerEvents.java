package me.jesper.sch.jeconomy.player;

import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {
    private JEconomy plugin = JEconomy.getPlugin();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.mongoConnect.addNewPlayer(player);
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.playerManagerHashMap.remove(player.getUniqueId());
    }
}
