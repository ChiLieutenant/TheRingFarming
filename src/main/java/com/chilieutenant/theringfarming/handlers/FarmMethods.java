package com.chilieutenant.theringfarming.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FarmMethods {

    public static void createFarm(Player player, String no, double rent, ItemStack itemStack){
        if(Farm.isFarmCreated(no)){
            player.sendMessage(ChatColor.RED + "Bu no ile bir ev zaten bulunmakta.");
            return;
        }
        if(!player.hasPermission("farm.admin")){
            player.sendMessage(ChatColor.RED + "Bunu yapmak için yetkin yok.");
            return;
        }

        new Farm(no, itemStack, rent, player.getEyeLocation());
        player.sendMessage(ChatColor.GREEN + "Başarıyla bir tarla oluşturdunuz. No: " + no);
    }


}
