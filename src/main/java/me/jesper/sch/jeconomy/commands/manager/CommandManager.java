package me.jesper.sch.jeconomy.commands.manager;

import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.commands.*;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandManager implements CommandExecutor {

    private ArrayList<SubCommand> commands = new ArrayList<SubCommand>();
    private JEconomy plugin = JEconomy.getPlugin();

    //Sub Commands
    private String main = "jeconomy";

    public void setup() {
        plugin.getCommand(main).setExecutor(this);
        plugin.getCommand("balance").setExecutor(new BalanceCommand());
        plugin.getCommand("pay").setExecutor(new PayCommand());
        plugin.getCommand("bounty").setExecutor(new BountyCommand());
        plugin.getCommand("shop").setExecutor(new ShopCommand());
        plugin.getCommand("auction").setExecutor(new AuctionCommand());
        plugin.getCommand("upgrade").setExecutor(new UpgradeCommand());
        plugin.getCommand("sell").setExecutor(new SellCommand());
        this.commands.add(new CreatePlayerAccountCommand());

    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase(main)) {
                if (args.length == 0) {
                    if(sender instanceof Player){
                    MessageManager.playerInfo(player, "Je hebt niet alle benodigde argumenten gegeven. Gebruik '/" + main + " help'  voor hulp");
                    return true;
                    } else{ MessageManager.consoleInfo("You forgot arguments.");}
                }

                SubCommand target = this.get(args[0]);
                if (target == null) {
                    MessageManager.playerBad(player, "/" + main + " " + args[0] + " is not a valid subcommand!");
                    return true;
                }

                ArrayList<String> a = new ArrayList<String>();
                a.addAll(Arrays.asList(args));
                a.remove(0);
                args = a.toArray(new String[a.size()]);

                try {
                    target.onCommand(player, args);
                } catch (Exception var9) {
                    MessageManager.playerBad(player, "An error has occured: " + var9.getCause());
                    var9.printStackTrace();
                }
            }

        }
        return true;

    }

    private SubCommand get(String name) {

        for (SubCommand cmd : this.commands) {
            if (cmd.name().equalsIgnoreCase(name)) {
                return cmd;
            }

            String[] var1;
            int var6 = (var1 = cmd.aliases()).length;

            for (int var5 = 0; var5 < var6; ++var5) {
                String alias = var1[var5];
                if (name.equalsIgnoreCase(alias)) {
                    return cmd;
                }
            }
        }

        return null;
    }
}
