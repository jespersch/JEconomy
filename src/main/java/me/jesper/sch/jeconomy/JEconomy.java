package me.jesper.sch.jeconomy;

import me.jesper.sch.jeconomy.commands.AuctionCommand;
import me.jesper.sch.jeconomy.commands.SellCommand;
import me.jesper.sch.jeconomy.commands.ShopCommand;
import me.jesper.sch.jeconomy.commands.UpgradeCommand;
import me.jesper.sch.jeconomy.commands.manager.CommandManager;
import me.jesper.sch.jeconomy.player.PlayerEconomyEvents;
import me.jesper.sch.jeconomy.player.PlayerEvents;
import me.jesper.sch.jeconomy.player.PlayerManager;
import me.jesper.sch.jeconomy.utils.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import me.jesper.sch.jeconomy.utils.MongoConnect;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class JEconomy extends JavaPlugin {
    private static JEconomy plugin;
    public MongoConnect mongoConnect;
    private CommandManager commandManager;
    private String prefix = MessageManager.getPrefix();
    public EconomyCore economyCore;
    public HashMap<UUID, PlayerManager> playerManagerHashMap = new HashMap<>();
    public static JEconomy getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        if(!new File(getDataFolder(), "config.yml").exists()){
            saveDefaultConfig();
            reloadConfig();
        }
        instanceClasses();
        mongoConnect.connect();
        commandManager.setup();
        if(!setupEconomy()){
            MessageManager.consoleBad("Kon economy niet registreren, Installeer Vault.");
            getServer().getPluginManager().disablePlugin(this);
        }
        MessageManager.consoleGood(prefix + "Succesvol opgestart.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            mongoConnect.addNewPlayer(player);
        }

    }

    private boolean setupEconomy(){
        if(getServer().getPluginManager().getPlugin("Vault") == null){
            MessageManager.consoleBad(prefix + "Kon economy niet registreren, Installeer Vault.");
            return false;
        }
        getServer().getServicesManager().register(Economy.class, economyCore, this, ServicePriority.Highest);
        MessageManager.consoleGood(prefix + "Economy is geregistreerd.");
        return true;
    }

    public void instanceClasses(){

        playerManagerHashMap = new HashMap<>();
        mongoConnect = new MongoConnect();
        commandManager = new CommandManager();
        economyCore = new EconomyCore();

        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerEconomyEvents(), this);
        getServer().getPluginManager().registerEvents(new SellCommand(), this);
        getServer().getPluginManager().registerEvents(new AuctionCommand(), this);
        getServer().getPluginManager().registerEvents(new ShopCommand(), this);
        getServer().getPluginManager().registerEvents(new UpgradeCommand(), this);

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
