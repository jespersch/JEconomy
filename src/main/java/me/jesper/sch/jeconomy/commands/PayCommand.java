package me.jesper.sch.jeconomy.commands;

import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    private JEconomy plugin = JEconomy.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            double balance = plugin.economyCore.getBalance(player.getUniqueId().toString());
            int val = (int) balance;
            if(args.length == 2){
                String playerName = args[0];
                int amount = Integer.parseInt(args[1]);
                if(Bukkit.getPlayer(playerName) != null){
                    plugin.economyCore.withdrawPlayerToP(player.getUniqueId().toString(), amount, playerName);
                    plugin.economyCore.depositPlayerFromP(Bukkit.getPlayer(playerName).getUniqueId().toString(), amount, player.getName());
                } else{
                    MessageManager.playerBad(player, "Speler bestaat niet of is niet online.");
                }
            }
        }
        return true;
    }
}
