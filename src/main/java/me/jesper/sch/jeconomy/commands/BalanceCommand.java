package me.jesper.sch.jeconomy.commands;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.commands.manager.SubCommand;
import me.jesper.sch.jeconomy.player.PlayerManager;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.sun.tools.javac.util.Constants.format;

public class BalanceCommand implements CommandExecutor {
    private JEconomy plugin = JEconomy.getPlugin();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private MongoCollection bounty;
    private MongoDatabase db;
    public String bold = ChatColor.BOLD + "";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        db = plugin.mongoConnect.getDb();
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                if(args[0].equals("top")){
                    bounty = db.getCollection("PlayerData");
                    FindIterable<Document> iterDoc = bounty.find().sort(descending("balance"));
                    MongoCursor<Document> cursor = iterDoc.iterator();
                    MessageManager.playerNeutral(player, ChatColor.AQUA + bold + "Balance top:");
                    int pos = 1;
                    while (cursor.hasNext()){
                        String string = cursor.next().toString();
                        String parts[] = string.split(",");
                        // Extract name from datastring
                        String name1[] = parts[4].split("=");
                        String name = name1[1].replace("}", "");
                        // Extract balance from datastring
                        String balance1[] = parts[2].split("=");
                        String balance = balance1[1];
                        double balancePlayer = Double.parseDouble(balance);


                        if(pos == 1){
                            MessageManager.playerInfo(player, ChatColor.GOLD + bold + "" + pos+ ". " + name + ": " + ChatColor.YELLOW + "€" + df.format(balancePlayer));
                            pos++;
                        } else if (pos == 2){
                            MessageManager.playerInfo(player, ChatColor.GRAY + bold+ "" + pos+ ". " + name + ": " + ChatColor.YELLOW + "€" + df.format(balancePlayer));
                            pos++;
                        } else if (pos == 3){
                            MessageManager.playerInfo(player, ChatColor.DARK_RED + bold+ "" + pos+ ". " + name + ": " + ChatColor.YELLOW + "€" + df.format(balancePlayer));
                            pos++;
                        } else if (name.equals("Romee_SCH")){
                            MessageManager.playerNeutral(player, ChatColor.LIGHT_PURPLE + bold+ "" + pos+ ". " + name + ": " + ChatColor.YELLOW + "€" + df.format(balancePlayer));
                            pos++;
                        }else {
                            MessageManager.playerNeutral(player, "" + pos+ ". " + name + ": " + ChatColor.YELLOW + "€" + df.format(balancePlayer));
                            pos++;
                        }

                    }/*MessageManager.consoleGood("Totaal: " + bounty);
                    MessageManager.playerInfo(player, ChatColor.LIGHT_PURPLE + ": " + ChatColor.YELLOW + "€" + bounty);*/
                    return true;
                }

                MessageManager.playerBad(player, command());
                MessageManager.playerBad(player, command() + " <player>");
                return true;
            }

            double balance = plugin.economyCore.getBalanceFromP(player);
            MessageManager.playerGood(player, ChatColor.AQUA + "Je hebt: " + ChatColor.GREEN + "$" + df.format(balance));
        }
        return true;
    }

    private String command(){
        return "/balance";
    }
}
