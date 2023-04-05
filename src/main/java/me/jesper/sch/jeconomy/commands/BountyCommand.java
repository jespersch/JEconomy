package me.jesper.sch.jeconomy.commands;

import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import me.jesper.sch.jeconomy.utils.MongoConnect;
import net.milkbowl.vault.chat.Chat;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mongodb.client.result.InsertOneResult;
import org.json.simple.JSONObject;

import java.util.Iterator;

import static com.mongodb.client.model.Filters.*;

public class BountyCommand implements CommandExecutor {
    private JEconomy plugin = JEconomy.getPlugin();
    private MongoCollection bounty;
    private MongoDatabase db;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        db = plugin.mongoConnect.getDb();
        if(args.length == 0){
            bounty = db.getCollection("Bounty");
            Document document = new Document("player", player.getName());
            FindIterable<Document> iterDoc = bounty.find(document);
            MongoCursor<Document> cursor = iterDoc.iterator();
            double bounty = 0;
                while (cursor.hasNext()){
                    String string = cursor.next().toString();
                        String[] parts = string.split(",");
                        String[] parts2 = parts[1].split("=");
                        bounty = bounty + Double.parseDouble(parts2[1]);

                    }
                MessageManager.consoleGood("Totaal: " + bounty);
                MessageManager.playerBad(player, "Er staat " + ChatColor.YELLOW + "€" + bounty + ChatColor.RED + " op je hoofd.");
        } else if (args.length == 1 ){
            if (args[0].equals("top")) {
                for (Player p: plugin.getServer().getOnlinePlayers()) {
                    String name = p.getName();
                        MessageManager.consoleInfo(name);
                        bounty = db.getCollection("Bounty");
                        Document document = new Document("player", name);
                        FindIterable<Document> iterDoc = bounty.find(document);
                        MongoCursor<Document> cursor = iterDoc.iterator();
                    double bounty = 0;
                        while (cursor.hasNext()){
                            String string = cursor.next().toString();
                            MessageManager.consoleInfo(string);
                            String[] parts = string.split(",");
                            String[] parts2 = parts[1].split("=");
                            bounty = bounty + Double.parseDouble(parts2[1]);
                        }MessageManager.consoleGood("Totaal: " + bounty);
                        MessageManager.playerInfo(player, ChatColor.LIGHT_PURPLE + name + ": " + ChatColor.YELLOW + "€" + bounty);
                }
            } else {MessageManager.playerBad(player, "Onjuist commando, gebruik " + ChatColor.AQUA + "/bounty top" + ChatColor.RED + " of " + ChatColor.AQUA + "/bounty <player> <amount>");}
        }else if(args.length == 2) {
            String playerName = args[0];
            int amount = Integer.parseInt(args[1]);
            if (plugin.economyCore.getBalance(player.getUniqueId().toString()) >= amount) {
                if (Bukkit.getPlayer(playerName) != null) {
                    MessageManager.playerGood(player, ChatColor.AQUA + "Je hebt " + ChatColor.YELLOW + "€" + amount + ChatColor.AQUA + " betaald voor een bounty op " + ChatColor.LIGHT_PURPLE + playerName + ChatColor.AQUA + ".");
                    plugin.economyCore.withdrawPlayerNoMSG(player.getUniqueId().toString(), amount);

                    String uuid = Bukkit.getPlayer(playerName).getUniqueId().toString();
                    insertBounty(playerName, amount);

                    Bukkit.broadcastMessage(ChatColor.AQUA + "Er is een bounty van: " + ChatColor.YELLOW + "€" +amount + ChatColor.AQUA + " op " + ChatColor.LIGHT_PURPLE + playerName + ChatColor.AQUA + " gezet door " + ChatColor.LIGHT_PURPLE + player.getName());

                } else {
                    MessageManager.playerBad(player, "Speler bestaat niet of is niet online.");
                }
            } else {MessageManager.playerBad( player, "Je hebt niet genoeg geld om deze bounty te zetten.");}
        }else{
            MessageManager.playerBad(player, "/bounty <player> <amount>");
        }
        return true;
    }
    public void insertBounty(String player, double amount){
        db = plugin.mongoConnect.getDb();
        try {
        db.getCollection("Bounty").insertOne(new Document()
                .append("bounty", amount)
                .append("player", player));
    } catch (MongoException e) {
        MessageManager.consoleBad(e.toString());
    };}
}
