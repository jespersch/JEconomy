package me.jesper.sch.jeconomy.utils;

import com.mongodb.Mongo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.player.PlayerManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MongoConnect {
    private MongoDatabase db;
    private JEconomy plugin = JEconomy.getPlugin();
    private MongoCollection playerData;

    public void connect(){
        MongoClient client = MongoClients.create("mongodb+srv://jesperschilte:Jesper17389832@cluster.49d4p2g.mongodb.net/?retryWrites=true&w=majority");
        setDb(client.getDatabase("JEconomy"));
        setPlayerDataCollection(db.getCollection("PlayerData"));
        MessageManager.consoleGood("Database connected.");

    }

    public void loadPlayerData(Player player){
        Document document = (Document) plugin.mongoConnect.getPlayerDataCollection()
                .find(new Document("uuid", player.getUniqueId().toString())).first();

        double balance = document.getDouble("balance");
        double bank = document.getDouble("bank-account");
        plugin.playerManagerHashMap.put(player.getUniqueId(),
                new PlayerManager(player.getUniqueId().toString(), balance, bank, player.getName()));
    }

    public void addNewPlayer(Player player){
        if(getPlayerDataCollection().find(new Document("uuid", player.getUniqueId().toString())).first() == null){
            plugin.playerManagerHashMap.put(player.getUniqueId(),
                    new PlayerManager(player.getUniqueId().toString(), 0, 0, player.getName()));
            MessageManager.playerGood(player, ChatColor.AQUA + "JEconomy" + ChatColor.GREEN + " account has been created.");
        } else {
            MessageManager.playerBad(player, "Gegevens opgehaald");
            loadPlayerData(player);
        }
    }

    public void setPlayerDataDocument(Object value, String identifier, String uuid) {
        Document document = new Document("uuid", uuid);
        Bson newValue = new Document(identifier, value);
        Bson updateOperation = new Document("$set", newValue);
        playerData.updateOne(document, updateOperation);
    }

    public Object getPlayerDataDocument(String identifier, String uuid) {
        Document document = new Document("uuid", uuid);
        if (playerData.find(document).first() != null) {
            Document found = (Document) playerData.find(document).first();
            return found.get(identifier);
        }

        MessageManager.consoleBad("Document is null for UUID: " + uuid);
        return null;
    }

    public MongoCollection getPlayerDataCollection() {
        return playerData;
    }

    public void setPlayerDataCollection(MongoCollection playerData) {
        this.playerData = playerData;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void setDb(MongoDatabase db) {
        this.db = db;
    }
}
