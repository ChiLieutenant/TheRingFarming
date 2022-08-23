package com.chilieutenant.theringfarming.handlers;

import com.chilieutenant.theringfarming.Main;
import com.chilieutenant.theringfarming.utils.Utils;
import com.chilieutenant.theringjobs.handlers.Jobs;
import com.chilieutenant.theringlabor.handlers.Bank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FarmCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(args.length > 0){
            if(args[0].equalsIgnoreCase("create") && player.hasPermission("farm.admin")){
                if(args.length < 3){
                    player.sendMessage(ChatColor.RED + "Doğru kullanım: /farm create (no) (rent)");
                    return false;
                }
                String no = args[1];
                if(!Utils.isNumeric(args[2])){
                    player.sendMessage(ChatColor.RED + "Doğru kullanım: /farm create (no) (rent)");
                    return false;
                }

                double rent = Double.parseDouble(args[2]);

                FarmMethods.createFarm(player, no, rent, player.getInventory().getItemInMainHand());
            }
            if(args[0].equalsIgnoreCase("remove") && player.hasPermission("farm.admin")){
                if(args.length < 2){
                    player.sendMessage(ChatColor.RED + "Doğru kullanım: /farm remove (no)");
                    return false;
                }
                String no = args[1];
                if (!Farm.isFarmCreated(no)) {
                    player.sendMessage(ChatColor.RED + "Bu no ile bir tarla bulunmamakta.");
                    return false;
                }
                Farm farm = new Farm(no);

                farm.remove();
                player.sendMessage(ChatColor.GREEN + "Başarıyla tarlayı sildiniz.");
            }
            if(args[0].equalsIgnoreCase("tp") && player.hasPermission("farm.admin")){
                if(args.length < 2){
                    player.sendMessage(ChatColor.RED + "Doğru kullanım: /farm tp (no)");
                    return false;
                }
                String no = args[1];
                if (!Farm.isFarmCreated(no)) {
                    player.sendMessage(ChatColor.RED + "Bu no ile bir tarla bulunmamakta.");
                    return false;
                }
                Farm house = new Farm(no);
                player.teleport(house.getHologramLocation());
                player.sendMessage(ChatColor.GREEN + "Başarıyla eve ışınlandınız.");
            }
            if (args[0].equalsIgnoreCase("info")) {
                if (args.length < 2) return false;
                String no = args[1];
                if (!Farm.isFarmCreated(no)) return false;
                Farm farm = new Farm(no);

                farm.sendMessage(player);
            }
            if (args[0].equalsIgnoreCase("addnode") && player.hasPermission("farm.admin")) {
                if (args.length < 2) return false;
                String no = args[1];
                if (!Farm.isFarmCreated(no)) return false;
                Farm farm = new Farm(no);
                farm.addNode(player.getEyeLocation());
            }
            if (args[0].equalsIgnoreCase("payrent")) {
                if(args.length < 2) return false;
                String no = args[1];
                if(!Farm.isFarmCreated(no)) return false;
                Farm farm = new Farm(no);

                if(!farm.hasOwner()) return false;
                if(!farm.getOwner().equals(player)){
                    player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Bu tarlanın kirasını ödemek için ev sahibi olmanız gerekli.");
                    return false;
                }
                if(!Main.econ.has(player, farm.getRent())){
                    player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Yeterli paranız bulunmamakta!");
                    return false;
                }
                farm.payRent();
                Main.econ.withdrawPlayer(player, farm.getRent());
                Bank.addMoney("rohan", farm.getRent());
                player.sendMessage(Utils.getPrefix() + ChatColor.GREEN + "Başarıyla " + no + " nolu tarlanın kirasını ödediniz!");
            }
            if (args[0].equalsIgnoreCase("buy")) {
                if(args.length < 2) return false;
                String no = args[1];
                if(!Farm.isFarmCreated(no)) return false;
                Farm farm = new Farm(no);

                if(farm.hasOwner()){
                    player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Bu tarlanın zaten bir sahibi var.");
                    return false;
                }
                if(!Main.econ.has(player, farm.getRent())){
                    player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Yeterli paranız bulunmamakta! Gerekli para: " + farm.getRent());
                    return false;
                }
                if(!Jobs.getJob(player).equalsIgnoreCase("çiftçi")){
                    player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Tarla kiralamak için çiftçi olmak zorundasınız.");
                    return false;
                }
                farm.buy(player);
                Main.econ.withdrawPlayer(player, farm.getRent());
                Bank.addMoney("rohan", farm.getRent());
            }
        }
        return false;
    }
}
