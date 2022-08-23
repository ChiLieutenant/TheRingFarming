package com.chilieutenant.theringfarming.handlers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FarmTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        if (args.length == 1) {
            if(sender instanceof Player){
                Player player = (Player) sender;
                if(player.hasPermission("farm.admin")){
                    commands.add("create");
                    commands.add("rent");
                    commands.add("tp");
                }
            }
            commands.add("info");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2){
            if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rent") || args[0].equalsIgnoreCase("tp")) {
                commands.addAll(Farm.data.getData().keySet());
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}
