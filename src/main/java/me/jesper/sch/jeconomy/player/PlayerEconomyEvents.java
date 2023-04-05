package me.jesper.sch.jeconomy.player;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;

public class PlayerEconomyEvents implements Listener {
    private JEconomy plugin = JEconomy.getPlugin();
    private MongoCollection bounty;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private MongoDatabase db;
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){

        Player player = e.getPlayer();
        if(e.getBlockPlaced().getType() == Material.SPAWNER) {
            ItemStack b = new ItemStack(Material.SPAWNER);
            ItemMeta spawnerMeta = b.getItemMeta();
            BlockData data = Material.SPAWNER.createBlockData();

            ((CreatureSpawner) data).setSpawnedType(EntityType.CREEPER);
            ((BlockDataMeta) spawnerMeta).setBlockData(data);
            b.setItemMeta(spawnerMeta);

            player.getInventory().addItem(b);

            MessageManager.playerGood(player, "Block placed");
        }
    }
    @EventHandler
    public void onEntityKilled(EntityDeathEvent e){
        if(e.getEntity().getKiller() instanceof Player) {
            Player player = e.getEntity().getKiller();
            Entity entity = e.getEntity();

            double foundCoins = Math.random();
            MessageManager.playerGood(player, "The " + entity.getName() + " dropped: " + ChatColor.YELLOW +  df.format(foundCoins));
            plugin.economyCore.depositPlayerNoMSG(player.getUniqueId().toString(), foundCoins);
        }
    }
    @EventHandler
    public void onPlayerKilled(PlayerDeathEvent deathEvent){
        db = plugin.mongoConnect.getDb();
        Player player = deathEvent.getEntity().getPlayer();
        Player killer = deathEvent.getEntity().getKiller();
        if(killer instanceof Player){
            bounty = db.getCollection("Bounty");
            Document doc = new Document("player", player.getName());
            FindIterable<Document> iterDoc = bounty.find(doc);
            MongoCursor<Document> cursor = iterDoc.iterator();
            double bounty = 0;
            while (cursor.hasNext()){
                String string = cursor.next().toString();
                MessageManager.consoleInfo(string);
                String[] parts = string.split(",");
                String[] parts2 = parts[1].split("=");
                bounty = bounty + Double.parseDouble(parts2[1]);
            }
            if(bounty > 0) {

                plugin.economyCore.depositPlayer(killer.getUniqueId().toString(), bounty);
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    MessageManager.playerGood(p,
                            "Bounty van: " + ChatColor.YELLOW + "€" + bounty + ChatColor.AQUA + " op "
                                    + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.AQUA + " is geclaimed door " + ChatColor.LIGHT_PURPLE + killer.getName());
                }
                db.getCollection("Bounty").deleteMany(doc);
            }
        }
    }
}
