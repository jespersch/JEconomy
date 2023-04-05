package me.jesper.sch.jeconomy.commands;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import me.jesper.sch.jeconomy.EconomyCore;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AuctionCommand implements CommandExecutor, Listener{
    private JEconomy plugin = JEconomy.getPlugin();
    private static AuctionCommand auctionplugin;
    private EconomyCore economy = plugin.economyCore;
    private MongoCollection auctionItems;
    private MongoDatabase db;


    Inventory inv = Bukkit.createInventory(null, 36, "Auction");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        db = plugin.mongoConnect.getDb();
        if(sender instanceof Player){
            if(args.length == 0) {
                Player player = (Player) sender;
                setupAuction();
                openAuction(player);
            }
            if (args.length == 2){
                Player player = (Player) sender;
                Inventory playerInv = player.getInventory();
                if (args[0].equals("create")){
                    int leftLimit = 97; // letter 'a'
                    int rightLimit = 122; // letter 'z'
                    int targetStringLength = 10;
                    Random random = new Random();

                    String generatedString = random.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();

                    int price = Integer.parseInt(args[1]);
                    ItemStack auctionItem = player.getInventory().getItemInMainHand();
                    int aItemAmount = player.getInventory().getItemInMainHand().getAmount();

                    if(auctionItem != null){
                        MessageManager.playerNeutral(player, "Je hebt " + ChatColor.GREEN+ aItemAmount + " "+ ChatColor.GRAY+ auctionItem.getType().name() + ChatColor.AQUA + " Op de auction gezet.");
                        playerInv.removeItem(auctionItem);
                        insertAuctionItem(player.getName(), auctionItem.getType().toString(), auctionItem.getAmount(), price, generatedString);
                    }

                }
            }

        }

        return true;
    }

    public AuctionCommand getAuctionPlugin(){return auctionplugin;};

    public void setupAuction(){
    }

    void openAuction(HumanEntity ent){
        db = plugin.mongoConnect.getDb();
        Player player = (Player) ent;
        auctionItems = db.getCollection("Auction");
        FindIterable<Document> iterDoc = auctionItems.find();
        MongoCursor<Document> cursor = iterDoc.iterator();

        int o = 0;
        inv.clear();
        while (cursor.hasNext()){
            ArrayList list = new ArrayList();
            String string = cursor.next().toString();
            String[] parts = string.split(",");
            String[] parts2 = parts[1].split("=");
            for(int i = 0; i< parts.length; i++){
                String[] parts3 = parts[i].split("=");
                list.add(parts3[1]);
            }


            Material item = Material.matchMaterial(parts2[1]);
            String itemName = item.toString().replace("_", " ");
            String seller = list.get(4).toString();
            String Jid = list.get(5).toString().replace("}", " ").replace(")", " ");
            ItemStack a = new ItemStack(createMenuItem(item, itemName, "Amount: "+ list.get(2), "Price: " + ChatColor.YELLOW + list.get(3), ChatColor.LIGHT_PURPLE + "Seller: " + seller, "ID: " +Jid));
            o++;
            if(!inv.contains(a)){

                inv.addItem(a);
            }

        }

        ent.openInventory(inv);
    }

    @EventHandler
    public void onAuctionItemClick(InventoryClickEvent e){;
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals("Auction")){
            ItemStack clickedItem = e.getCurrentItem();
            if(clickedItem == null || !clickedItem.getType().isItem()) return;
            String itemName = clickedItem.getType().toString();

            e.setCancelled(true);
            buyItemFromAuc(p, clickedItem);
            e.setCancelled(true);

        }


    }
    public void buyItemFromAuc(Player p, ItemStack item){
        // Itemstack wordt meegevoerd, pak nu de value uit de lore
        String parts[] = item.getItemMeta().getLore().toString().split(",");

        // Pak amount uit Lore
        String itemsAmount = parts[0];
        String items1[] = itemsAmount.split(":");
        String items2 = ChatColor.stripColor(items1[1]);
        items2 = items2.replace(" ", "");
        int itemAmount = Integer.parseInt(items2);
        // Pak price uit lore
        String fullprice = parts[1];
        String pricehalf[]  = fullprice.split(":");
        String correctPrice = ChatColor.stripColor(pricehalf[1]);
        // pak seller uit lore
        String seller = parts[2];
        String seller2[] = seller.split(":");
        //pak id uit lore
        String ID = parts[3];


        String ID2[] = ID.replace("]", "").replace(" ", "").split(":");
        String Seller = seller2[1].replace(" ", "");
        double price = Double.parseDouble(correctPrice);
        double balance = economy.getBalance(p.getUniqueId().toString());
        List<String> players = new ArrayList<String>();
        for(Player player : Bukkit.getOnlinePlayers()){
            players.add(player.getName());
        }
        if(balance >= price){
            try {
                item = new ItemStack(item.getType(), itemAmount);

                openAuction(p);

                if(players.contains(Seller)){
                    Player s = Bukkit.getPlayer(Seller);
                    p.getInventory().addItem(item);
                    economy.withdrawPlayerNoMSG(p.getUniqueId().toString(), price);

                    economy.depositPlayerNoMSG(s.getUniqueId().toString(), price);
                    deleteAuctionItem(ID2[1]);
                    MessageManager.playerNeutral(p, "Je hebt een " + ChatColor.GREEN + item.getType().name() + ChatColor.AQUA + " gekocht voor: " +ChatColor.YELLOW + "€"+ price);
                    MessageManager.playerNeutral(s, "Jouw "+ ChatColor.YELLOW + item.getType().name() + ChatColor.AQUA +" in de auction is verkocht voor: " + ChatColor.YELLOW + "€" + price);
                } else{
                    MessageManager.playerNeutral(p, "Seller is niet online.");
                    ConfigurationSection cs = plugin.getConfig().createSection("missedMoney");
                    cs.set("Player."+ Seller, price);

                }
            } catch(Exception e){
                MessageManager.consoleBad(e.toString());
            }
            p.closeInventory();
            openAuction(p);
        } else {
            MessageManager.playerBad(p, "Je hebt niet genoeg geld om dit te kopen.");
        }

    }


    public void insertAuctionItem(String player, String itemType, int amount, double price, String generatedString){
        db = plugin.mongoConnect.getDb();
        try {
            db.getCollection("Auction").insertOne(new Document()
                    .append("item", itemType)
                    .append("amount", amount)
                    .append("price", price)
                    .append("seller", player)
                    .append("Jid", generatedString));
        } catch (MongoException e) {
            MessageManager.consoleBad(e.toString());
        };}

    public void deleteAuctionItem(String generatedString){
        db = plugin.mongoConnect.getDb();
        Document filter = new Document("Jid", generatedString);
        db.getCollection("Auction").deleteOne(filter);
    }

    protected ItemStack createMenuItem(Material item, String name,  String... lore){

        final ItemStack auctionItem = new ItemStack(item, 1);
        ItemMeta meta = auctionItem.getItemMeta();

        meta.setDisplayName(ChatColor.UNDERLINE + name);

        meta.setLore(Arrays.asList(lore));

        auctionItem.setItemMeta(meta);

        return auctionItem;
    }
    public int getValue(ItemStack item){
        FileConfiguration config = plugin.getConfig();

        String value = "0";
        String itemname = item.toString();
        int itemValue = Integer.parseInt(value);


        return itemValue;
    }

}
