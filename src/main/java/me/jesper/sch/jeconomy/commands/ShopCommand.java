package me.jesper.sch.jeconomy.commands;

import me.jesper.sch.jeconomy.EconomyCore;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ShopCommand implements CommandExecutor, Listener {
    private JEconomy plugin = JEconomy.getPlugin();
    private static ShopCommand shopplugin;
    private EconomyCore economy = plugin.economyCore;

    Inventory inv = Bukkit.createInventory(null, 36, "Shop");
    Inventory spawner = Bukkit.createInventory(null, 36, "Spawners");
    Inventory misc = Bukkit.createInventory(null, 36, "Misc");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            setupShop();
            openShop(player);

        }
        return true;
    }

    public ShopCommand getShopPlugin(){return shopplugin;};

    public void setupShop(){
        initShopItems();
    }

    void openShop(HumanEntity ent){
        ent.openInventory(inv);
    }

    public void openSectionMisc(HumanEntity ent){
        initSectionMiscItems();
        ent.openInventory(misc);
    }
    public void openSectionSpawners(HumanEntity ent){
        initSectionSpawnerItems();
        ent.openInventory(spawner);
    }

    @EventHandler
    public void onShopItemClick(InventoryClickEvent e){;
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals("Shop")){

            ItemStack clickedItem = e.getCurrentItem();
            String itemName = clickedItem.getType().toString();

            if(clickedItem.getType() == Material.SPAWNER){
                openSectionSpawners(p);
            }
            if(clickedItem.getType() == Material.BOOK){
                openSectionMisc(p);
            }

            e.setCancelled(true);
        }

        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null || !clickedItem.getType().isItem()) return;

    }
    @EventHandler
    public void onSectionItemClick(InventoryClickEvent e){;
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals("Spawners")){
            ItemStack clickedItem = e.getCurrentItem();
            String itemName = clickedItem.getType().toString();
            if(clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {e.setCancelled(true);} else{
                buyItem(p, e.getCurrentItem());}
            e.setCancelled(true);
        }
        if(e.getView().getTitle().equals("Misc")){
            ItemStack clickedItem = e.getCurrentItem();
            String itemName = clickedItem.getType().toString();
            if(clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {e.setCancelled(true);} else{

            buyItem(p, e.getCurrentItem());}


            e.setCancelled(true);
        }
        ItemStack clickedItem = e.getCurrentItem();

    }

    public void buyItem(Player p, ItemStack item){
        int price = getValue(item.getType());
        double balance = economy.getBalance(p.getUniqueId().toString());
        if(balance >= price){
            p.getInventory().addItem(item);
            economy.withdrawPlayerNoMSG(p.getUniqueId().toString(), price * item.getAmount());

            MessageManager.playerNeutral(p, "Je hebt een " + ChatColor.GREEN + item.getType().name() + ChatColor.AQUA + " gekocht voor: " +ChatColor.YELLOW + "€"+ price * item.getAmount());
        } else {
            MessageManager.playerBad(p, "Je hebt niet genoeg geld om dit te kopen.");
        }

    }

    public void initShopItems(){
        ItemStack spawner = createMenuItem(Material.SPAWNER, ChatColor.GOLD + "Spawners","Hier kan je een spawner/egg kopen");
        ItemStack misc = createMenuItem(Material.BOOK, ChatColor.GOLD + "Misc","Hier kan je een misc kopen");
        ItemStack fillItem = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, "", "");
        for(int slot = 0; slot < inv.getSize(); slot++) {
            if(inv.getItem(slot) == null) {
                inv.setItem(slot, fillItem);
            }
        }
        if(!inv.contains(spawner)){
            inv.setItem(12, spawner);}
        if(!inv.contains(misc)){
            inv.setItem(14, misc);}
    }
    public void initSectionSpawnerItems(){
        ItemStack spawneritem = createMenuItem(Material.SPAWNER, ChatColor.GOLD + "Spawner",ChatColor.YELLOW + "€" + getValue(Material.SPAWNER) ,"Nutteloos zonder ei");
        ItemStack creeperEgg = createMenuItem(Material.CREEPER_SPAWN_EGG, ChatColor.GREEN + "Creeper " + ChatColor.GOLD + "Spawner",ChatColor.YELLOW + "€" + getValue(Material.SPAWNER) ,"Gebruikt om spawners in te stellen");
        ItemStack fillItem = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, "", "");
        for(int slot = 0; slot < spawner.getSize(); slot++) {
            if(spawner.getItem(slot) == null) {
                spawner.setItem(slot, fillItem);
            }

        }
            spawner.clear();
            spawner.setItem(4, spawneritem);
            spawner.setItem(22, creeperEgg);

    }



    public void initSectionMiscItems(){
        ItemStack sectionitem = createMenuItem(Material.BOOK, ChatColor.AQUA + "Book",ChatColor.YELLOW + "€" + getValue(Material.BOOK));
        ItemStack fillItem = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, "", "");
        for(int slot = 0; slot < misc.getSize(); slot++) {
            if(misc.getItem(slot) == null) {
                misc.setItem(slot, fillItem);
            }

        }
        if(!misc.contains(sectionitem)){
            misc.setItem(4, sectionitem);}

    }

    protected ItemStack createMenuItem(Material item, String name,  String... lore){

        final ItemStack shopItem = new ItemStack(item, 1);
        ItemMeta meta = shopItem.getItemMeta();

        meta.setDisplayName(ChatColor.UNDERLINE + name);

        meta.setLore(Arrays.asList(lore));

        shopItem.setItemMeta(meta);

        return shopItem;
    }
    public int getValue(Material item){
        FileConfiguration config = plugin.getConfig();
        String value;
        String itemname = item.toString();
        value = config.getString(itemname + ".value");
        int itemValue = Integer.parseInt(value);


        return itemValue;
    }
}
