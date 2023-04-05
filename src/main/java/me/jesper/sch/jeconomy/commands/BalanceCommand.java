package me.jesper.sch.jeconomy.commands;

import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.commands.manager.SubCommand;
import me.jesper.sch.jeconomy.player.PlayerManager;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

import static com.sun.tools.javac.util.Constants.format;

public class BalanceCommand implements CommandExecutor {
    private JEconomy plugin = JEconomy.getPlugin();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length != 0) {
                MessageManager.playerBad(player, command());
                MessageManager.playerBad(player, command() + " <player>");
                return true;
            }

            double balance = plugin.economyCore.getBalanceFromP(player);
            if(player.getName().equals("WolfsHeaven")){MessageManager.playerBad(player, ChatColor.LIGHT_PURPLE + "Je hebt" + df.format(balance) + " kanker leijer");}else
            MessageManager.playerGood(player, ChatColor.AQUA + "Je hebt: " + ChatColor.GREEN + "$" + df.format(balance));
        }
        return true;
    }

    private String command(){
        return "/balance";
    }
}
