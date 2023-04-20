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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class UpgradeCommand implements CommandExecutor, Listener{
    private JEconomy plugin = JEconomy.getPlugin();
    private static UpgradeCommand auctionplugin;
    private EconomyCore economy = plugin.economyCore;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private MongoCollection auctionItems;
    private MongoDatabase db;


    Inventory inv = Bukkit.createInventory(null, 36, "Upgrade menu");
    ItemStack fortune = createMenuItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Fortune Upgrade","Current level * 15 + 15");
    ItemStack looting = createMenuItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Looting Upgrade","Current level * 15 + 15");
    ItemStack sweeping = createMenuItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Sweeping Edge Upgrade","Current level * 15 + 15");
    ItemStack damage = createMenuItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Sharpness Upgrade","100k + 25k per level");
    ItemStack info = createMenuItem(Material.BOOK, ChatColor.AQUA + "Levels examples", "Level 2 Looting = 1 * 15 + 15 = 30", "Level 3 looting = 2 * 15 + 15 = 45");


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){

            Player player = (Player) sender;
            if(args.length == 0) {
                ItemStack tool = new ItemStack(player.getInventory().getItemInMainHand());
                Map<Enchantment, Integer> a = tool.getEnchantments();

                Map<Enchantment, Integer> enchants = tool.getEnchantments();
                inv.clear();
                if(tool != null){
                    // Tools
                    if(tool.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS) > 0){
                        inv.setItem(13 , fortune);

                    }
                    // Swords
                    if(tool.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS) > 0){
                        inv.setItem(12 , looting);

                    }
                    if(tool.getItemMeta().getEnchantLevel(Enchantment.SWEEPING_EDGE) > 0){
                        inv.setItem(14 , sweeping);

                    }
                    if(tool.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL) > 0){
                        inv.setItem(13 , damage);

                    }
                    inv.setItem(35 , info);
                    openMenu(player);
                }
            } else {
                MessageManager.playerBad(player, "Je hebt geen tool vast.");
            }

        }

        return true;
    }

    void setupMenu(){

    }
    void openMenu(HumanEntity ent){
        ent.openInventory(inv);
    }

    public void enchantUpgrade(Player player, ItemStack tool, Enchantment ench, boolean levels){
        int currentLevelTool = tool.getEnchantmentLevel(ench);
        int levelsNeeded = (currentLevelTool * 15) + 15;
        int moneyNeeded = (currentLevelTool * 25000) + 100000;
        if (currentLevelTool > 0 && levels == true){
            if(player.getLevel() >= levelsNeeded){
                player.giveExpLevels(-levelsNeeded);
                tool.addUnsafeEnchantment(ench, currentLevelTool + 1);

                MessageManager.playerNeutral(player, "Jouw " + ChatColor.YELLOW +tool.getType() + ChatColor.AQUA +" is gestegen in een level van: " + ChatColor.LIGHT_PURPLE + ench.getKey());
            } else{
                MessageManager.playerBad(player, "Je hebt niet genoeg levels om jouw tool te upgraden.");
                MessageManager.playerInfo(player, "Benodigde level: " + levelsNeeded);
                MessageManager.playerNeutral(player, "Je huidige level: " + player.getLevel());
            }
        } else {
            MessageManager.playerBad(player, "Je hebt deze enchant niet.");
        }

        if (currentLevelTool > 0 && levels == false){
            if(economy.getBalance(player.getUniqueId().toString()) >= moneyNeeded){
                economy.withdrawPlayerNoMSG(player.getUniqueId().toString(), moneyNeeded);
                tool.addUnsafeEnchantment(ench, currentLevelTool + 1);

                MessageManager.playerNeutral(player, "Jouw " + ChatColor.YELLOW +tool.getType() + ChatColor.AQUA +" is gestegen in een level van: " + ChatColor.LIGHT_PURPLE + ench.getKey());
            } else{
                MessageManager.playerBad(player, "Je hebt niet genoeg geld om jouw tool te upgraden.");
                MessageManager.playerInfo(player, "Benodigde geld: " + moneyNeeded);
                MessageManager.playerNeutral(player, "Je huidige geld: " + df.format(economy.getBalance(player.getUniqueId().toString())));
            }
        }


    }

    @EventHandler
    public void onUpgradeMenuItemClick(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        ItemStack currentTool = player.getInventory().getItemInMainHand();
        if(e.getView().getTitle().equals("Upgrade menu")) {
            if(e.getCurrentItem().equals(fortune)){
                enchantUpgrade(player, currentTool, Enchantment.LOOT_BONUS_BLOCKS, true);
            }
            if(e.getCurrentItem().equals(looting)){
                enchantUpgrade(player, currentTool, Enchantment.LOOT_BONUS_MOBS, true);
            }
            if(e.getCurrentItem().equals(sweeping)){
                enchantUpgrade(player, currentTool, Enchantment.SWEEPING_EDGE, true);
            }
            if(e.getCurrentItem().equals(damage)){
                enchantUpgrade(player, currentTool, Enchantment.DAMAGE_ALL, false);
            }
            e.setCancelled(true);
            }

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
