package me.jesper.sch.jeconomy.commands;

import me.jesper.sch.jeconomy.JEconomy;
import me.jesper.sch.jeconomy.commands.manager.SubCommand;
import me.jesper.sch.jeconomy.player.PlayerManager;
import me.jesper.sch.jeconomy.utils.MessageManager;
import org.bson.Document;
import org.bukkit.entity.Player;

public class CreatePlayerAccountCommand extends SubCommand {
    private JEconomy plugin = JEconomy.getPlugin();

    @Override
    public void onCommand(Player player, String[] args) {
         boolean hasacc = plugin.mongoConnect.getPlayerDataCollection().find(new Document("uuid", player.getUniqueId().toString())).first() == null;
        if(hasacc){
            plugin.playerManagerHashMap.put(player.getUniqueId(), new PlayerManager(player.getUniqueId().toString(), 0, 0, player.getName()));
            MessageManager.playerGood(player, "Your account has been created.");
        } else{ MessageManager.playerBad(player, "Your account already exists.");}
    }

    @Override
    public String name() {
        return "createaccount";
    }

    @Override
    public String info() {
        return "This command is used to create an account for JEconomy";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
