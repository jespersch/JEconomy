package me.jesper.sch.jeconomy.commands;

import me.jesper.sch.jeconomy.EconomyCore;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SellCommand implements CommandExecutor, Listener {
    private JEconomy plugin = JEconomy.getPlugin();
    private static SellCommand shopplugin;
    private EconomyCore economy = plugin.economyCore;
    public String bold = ChatColor.BOLD + "";


    Inventory inv = Bukkit.createInventory(null, 36, "Sell");
    ItemStack SellWand = createMenuItem(Material.BLAZE_ROD, ChatColor.GREEN + bold + "Sell Wand", "Right-Click chest to sell its content");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(args.length == 0){
            setupSell();
            openShop(player);
            }else if(args.length == 1){
                if (args[0].equals("buywand")){
                    if(economy.getBalance(player.getUniqueId().toString()) >= 100000){
                        economy.withdrawPlayerNoMSG(player.getUniqueId().toString(), 100000);
                        MessageManager.playerGood(player, "Je hebt een sell wand gekocht");
                        SellWand.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
                        player.getInventory().addItem(SellWand);
                    } else {
                        MessageManager.playerBad(player, "Je hebt niet genoeg geld om de sellwand te kopen. 100.000");
                    }
                }
            }
        }
        return true;
    }

    public SellCommand getShopPlugin(){return shopplugin;};

    public void setupSell(){
        initSellItems();
    }

    public void initSellItems(){
        ItemStack item = createMenuItem(Material.BARRIER, "§CCancel","");
        ItemStack sellButton = createMenuItem(Material.LIME_STAINED_GLASS_PANE, "§aSell","");
        if(!inv.contains(item)){
        inv.setItem(27, item);}
        if(!inv.contains(sellButton)){
        inv.setItem(35, sellButton);}

    }
    protected ItemStack createMenuItem(Material item, String name,  String... lore){

        final ItemStack shopItem = new ItemStack(item, 1);
        ItemMeta meta = shopItem.getItemMeta();

        meta.setDisplayName(ChatColor.UNDERLINE + name);

        meta.setLore(Arrays.asList(lore));

        shopItem.setItemMeta(meta);

        return shopItem;
    }
    public void openShop(HumanEntity ent){
        ent.openInventory(inv);
    }

    public void Sell(ItemStack item){

    }

    @EventHandler
    public void onSellItemClick(InventoryClickEvent e){

        Inventory inv = e.getInventory();
        Player p = (Player) e.getWhoClicked();
        double amount = 0;
        if(e.getView().getTitle().equals("Sell")){
            if(e.getCurrentItem() != null){
                if(e.getCurrentItem().getType() == Material.BARRIER){
                    ItemStack spawner = new ItemStack(Material.SPAWNER);
                    for(ItemStack item : inv.getContents()){
                        if(item != null) {
                            if (item.getType().isItem() && !item.getType().equals(Material.BARRIER) && !item.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                                p.getInventory().addItem(item);
                                inv.remove(item);
                            }
                        }
                    }
                    e.setCancelled(true);
                    e.getWhoClicked().closeInventory();

                }
            }
            if(e.getCurrentItem() != null) {
                if (e.getCurrentItem().getType() == Material.LIME_STAINED_GLASS_PANE) {
                    e.setCancelled(true);
                    ArrayList<Integer> sales = new ArrayList<Integer>();
                    int totalSale = 0;
                    for (ItemStack item : inv.getContents()) {
                        if (item != null) {
                            if (item.getType().isItem() && !item.getType().equals(Material.BARRIER) && !item.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                                int saleAmount = getValue(item) * item.getAmount();
                                inv.remove(item);
                                sales.add(saleAmount);
                                e.setCancelled(true);
                            }
                        }
                    }
                    MessageManager.playerGood(p, sales.toString());
                    e.setCancelled(true);
                    for (Integer i : sales) {
                        totalSale = totalSale + i;
                    }
                    plugin.economyCore.depositPlayerNoMSG(p.getUniqueId().toString(), totalSale);
                    MessageManager.playerGood(p, "Je hebt je spullen verkocht voor: " + ChatColor.YELLOW + "€" + totalSale);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player p = (Player) event.getPlayer();
        if(event.getView().getTitle().equals("Sell")){
            for(ItemStack item : inv.getContents()){
                if(item != null) {
                    if (item.getType().isItem() && !item.getType().equals(Material.BARRIER) && !item.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                        p.getInventory().addItem(item);
                        inv.remove(item);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }

    public int getValue(ItemStack item){
        int itemValue = 0;
        FileConfiguration config = plugin.getConfig();
        String value;
        String itemname = item.getType().toString();
        value = config.getString(itemname + ".sell" );
        if(value != null) {
            itemValue = Integer.parseInt(value);
        }

    return itemValue;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getPlayer().getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
           if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
               e.setCancelled(true);
               if (block.getType() == Material.CHEST) {
                   BlockState state = block.getState();
                   Container cont = (Container) state;
                   ArrayList<Integer> sales = new ArrayList<Integer>();
                   int totalSale = 0;
                   for (ItemStack i : cont.getInventory()) {
                       if (i != null && i.getAmount() != 0 && i.getType() != Material.AIR) {
                           int saleAmount = getValue(i) * i.getAmount();
                           sales.add(saleAmount);
                       }

                   }
                   for (Integer i : sales) {
                       totalSale = totalSale + i;
                   }
                   MessageManager.playerGood(e.getPlayer(), "De inhoud van de kist is bevat: " + ChatColor.YELLOW + "€" + totalSale);
               }
           }
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                e.setCancelled(true);
                if (block.getType() == Material.CHEST) {
                    BlockState state = block.getState();
                    Container cont = (Container) state;
                    ArrayList<Integer> sales = new ArrayList<Integer>();
                    int totalSale = 0;
                    for (ItemStack i : cont.getInventory()) {
                        if (i != null && i.getAmount() != 0 && i.getType() != Material.AIR) {
                            int saleAmount = getValue(i) * i.getAmount();
                            sales.add(saleAmount);
                            if(saleAmount > 0){
                                cont.getInventory().removeItem(i);
                            }
                        }

                    }
                    for (Integer i : sales) {
                        totalSale = totalSale + i;
                    }
                    economy.depositPlayerNoMSG(e.getPlayer().getUniqueId().toString(), totalSale);
                    MessageManager.playerGood(e.getPlayer(), "Je hebt de inhoud verkocht voor: " + ChatColor.YELLOW + "€" + totalSale);
                }
            }
        }
    }

}
