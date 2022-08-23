package com.chilieutenant.theringfarming.handlers;

import com.chilieutenant.theringfarming.Main;
import com.chilieutenant.theringfarming.utils.CenteredText;
import com.chilieutenant.theringfarming.utils.Utils;
import com.chilieutenant.theringjobs.handlers.Jobs;
import com.chilieutenant.theringlabor.handlers.LaborPlayer;
import com.hakan.core.HCore;
import com.hakan.core.hologram.HHologram;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Farm {

    public static Yaml data = new Yaml("farms", "plugins/Farm");

    private String no;

    public Farm(String no){
        this.no = no;
    }

    public Farm(String no, ItemStack itemStack, double rent, Location location){
        this.no = no;
        data.set(no + ".item", Utils.itemToString(itemStack));
        data.set(no + ".rent", rent);
        data.set(no + ".owner", "");
        data.set(no + ".endTime", System.currentTimeMillis());
        data.set(no + ".growTime", System.currentTimeMillis());
        data.set(no + ".isGrowing", false);
        data.set(no + ".nodeSize", 0);
        data.set(no + ".location", Utils.getStringLocation(location));

        createHologram(location);
    }

    public void createHologram(Location location){
        if(location == null) return;
        Optional<HHologram> hologram = HCore.findHologramByID("farm" + no);
        hologram.ifPresent(HHologram::delete);
        setHologramLocation(location);
        HHologram myHologram = HCore.createHologram("farm" + no, location);
        myHologram.getRenderer().setRadius(4);
        myHologram.setLines(getLines());
        myHologram.showEveryone(true);
        myHologram.whenClicked((pl, line) -> sendMessage(pl));
    }

    public void setHologramLocation(Location location) {
        data.set(no + ".location", Utils.getStringLocation(location));
    }

    public Location getHologramLocation(){
        return Utils.getLocationString(data.getString(no + ".location"));
    }

    public void setNodeLocation(int i, Location location){
        data.set(no + "." + i + "nodeLocation", Utils.getStringLocation(location));
    }

    public Location getNodeLocation(int i){
        return Utils.getLocationString(data.getString(no + "." + i + "nodeLocation"));
    }

    public void loadHolograms(){
        if(getHologramLocation() == null) return;
        createHologram(getHologramLocation());
        for(int i = 0; i <= getNodeSize(); i++){
            createNodeHologram(i, getNodeLocation(i));
        }
    }

    public static boolean isFarmCreated(String no){
        return data.getData().containsKey(no);
    }

    public int getNodeSize(){
        return data.getInt(no + ".nodeSize");
    }

    public void createNodeHologram(int i, Location location){
        Optional<HHologram> hologram = HCore.findHologramByID("farm" + no + "node" + i);
        hologram.ifPresent(HHologram::delete);
        if(location == null) return;
        setNodeLocation(i, location);
        HHologram myHologram = HCore.createHologram("farm" + no + "node" + i, location);
        myHologram.getRenderer().setRadius(4);
        myHologram.setLines(getLines(i));
        myHologram.showEveryone(true);
        myHologram.whenClicked((pl, line) -> harvest(pl, i));
    }

    public void addNode(Location location){
        createNodeHologram(getNodeSize(), location);
        setGrowTime(getNodeSize(), System.currentTimeMillis() + (6 * 60 * 60 * 1000));
        data.set(no + ".nodeSize", getNodeSize() + 1);
    }

    public long getGrowTime(Player player){
        long growTime = 6 * 60 * 60 * 1000;
        int level = Jobs.getJobLevel(player);
        if(level > 17) growTime /= 0.06 * level;
        return growTime;
    }

    public void harvest(Player player, int node){
        if(!hasOwner()) return;
        if(!getOwner().equals(player)) return;
        if(isGrowing(node)) return;
        LaborPlayer laborPlayer = LaborPlayer.getLaborPlayer(player);
        ItemStack hoe = player.getInventory().getItemInMainHand();
        if(!hoe.getType().name().toLowerCase().contains("hoe")){
            player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Lütfen elinde bir çapa tut.");
            return;
        }
        if(laborPlayer.getLabor() < 300) {
            player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Yeterli iş gücün yok. Gereken iş gücü: 300");
            return;
        }
        laborPlayer.removeLabor(300);
        ItemStack item = getItem();
        item.setAmount(Utils.getTier(hoe) * item.getAmount());
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, hoe, item.getAmount());
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> {
            player.getLocation().getWorld().dropItem(player.getLocation(), item);
            Bukkit.getPluginManager().callEvent(event);
        }, 20);
        Jobs.addXP(player, (int) ((getGrowTime(player) / 1000) * 0.25));
        setGrowTime(node, System.currentTimeMillis() + getGrowTime(player));
    }

    public List<String> getLines(int node){
        List<String> list = new ArrayList<>();
        list.add(ChatColor.GRAY + "Mahsul: " + getItemName());
        list.add(ChatColor.BLUE + "Nokta: " + ChatColor.WHITE + node);
        list.add("");
        if(hasOwner()){
            if(isGrowing(node)){
                list.add(ChatColor.GRAY + "Bu noktada şuan üretim yapılıyor.");
                list.add(ChatColor.GRAY + "Hasat zamanına kalan süre: " + Utils.getMMSS(remainingGrowMins(node)) + " saat");
            }else{
                list.add(ChatColor.GRAY + "Sol tıklayarak hasat yapabilirsiniz.");
            }
        }
        return list;
    }

    public List<String> getLines(){
        List<String> list = new ArrayList<>();
        list.add(ChatColor.GRAY + "Tarla No: " + ChatColor.AQUA + no);
        if(!hasOwner()){
            list.add(ChatColor.GRAY + "Bu tarlayı kiralamak için sağ tıklayın.");
        }
        return list;
    }

    public void setGrowTime(int node, long time){
        data.set(no + "." + node + "growTime", time);
    }

    public long getGrowTime(int node){
        return data.getLong(no + "." + node + "growTime");
    }

    public long remainingGrowMins(int node){
        return (getGrowTime(node) - System.currentTimeMillis()) / (60 * 1000);
    }

    public boolean isGrowing(int node){
        return hasOwner() && remainingGrowMins(node) > 0;
    }

    public boolean hasOwner(){
        return !data.getString(no + ".owner").equalsIgnoreCase("");
    }

    public void setOwner(Player player){
        data.set(no + ".owner", player.getUniqueId().toString());
    }

    public OfflinePlayer getOwner(){
        return Bukkit.getOfflinePlayer(UUID.fromString(data.getString(no + ".owner")));
    }

    public void removeOwner(){
        data.set(no + ".owner", "");
    }

    public ItemStack getItem(){
        return Utils.stringToItem(data.getString(no + ".item"));
    }

    public double getRent() {
        return data.getDouble(no + ".rent");
    }

    public long getEndTime(){
        return data.getLong(no + ".endTime");
    }

    public double remainingHours(){
        return (double) (getEndTime() - System.currentTimeMillis()) / (60 * 60 * 1000);
    }

    public void remove(){
        Optional<HHologram> hologram = HCore.findHologramByID("farm" + no);
        hologram.ifPresent(HHologram::delete);
        for(int i = 0; i <= getNodeSize(); i++){
            Optional<HHologram> hologram1 = HCore.findHologramByID("farm" + no + "node" + i);
            hologram1.ifPresent(HHologram::delete);
        }
        data.remove(no);
    }

    public void buy(Player player){
        data.set(no + ".endTime", System.currentTimeMillis() + (48 * 60 * 60 * 1000));
        setOwner(player);
        for(int i = 0; i <= getNodeSize(); i++){
            setGrowTime(i, System.currentTimeMillis() + (6 * 60 * 60 * 1000));
        }
        player.sendMessage(Utils.getPrefix() + ChatColor.GREEN + "Tarla başarıyla kiralandı.");
        Optional<HHologram> hologram = HCore.findHologramByID("farm" + no);
        hologram.ifPresent((holo) -> holo.setLines(getLines()));
    }

    public void payRent(){
        data.set(no + ".endTime", getEndTime() + (48 * 60 * 60 * 1000));
    }

    public String getItemName(){
        if(!getItem().getItemMeta().hasDisplayName()){
            return getItem().getType().name();
        }else{
            return getItem().getItemMeta().getDisplayName();
        }
    }

    public void sendMessage(Player player){
        CenteredText.sendCenteredMessage(player, "&a&m                                                                    ");
        if(!hasOwner()){
            //buyable
            CenteredText.sendCenteredMessage(player, "&bTarla No: &7" + no);
            CenteredText.sendCenteredMessage(player, "&bKira: &7" + getRent() + " &f(48 saatlik)");
            CenteredText.sendCenteredMessage(player, "&bMahsul: &7" + getItemName());
            TextComponent message = new TextComponent(CenteredText.getCenteredMessage("&c[Kirala]"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Kiralamak için tıklayın.").create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/farm buy " + no));
            player.spigot().sendMessage(message);
        }else {
            CenteredText.sendCenteredMessage(player, "&bSahip: &7" + getOwner().getName());
            CenteredText.sendCenteredMessage(player, "&bMahsul: &7" + getItemName());
            if(getOwner().equals(player)){
                CenteredText.sendCenteredMessage(player, "&bKiranın bitiş süresi: &7" + String.format("%,.2f", remainingHours()) + " saat.");

                TextComponent message = new TextComponent(CenteredText.getCenteredMessage("&a[Kira Öde]"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Kirayı ödemek için tıklayın.").create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/farm payrent " + no));
                player.spigot().sendMessage(message);
            }
        }
        CenteredText.sendCenteredMessage(player, "&a&m                                                                    ");
    }

    public static void loadFarms(){
        for(String no : data.getData().keySet()){
            Farm farm = new Farm(no);
            farm.loadHolograms();
        }
    }

    public static void manageFarms(){
        new BukkitRunnable(){
            @Override
            public void run() {
                for(String no : data.getData().keySet()){
                    Farm house = new Farm(no);
                    if(!house.hasOwner()) continue;
                    for(int i = 0; i <= house.getNodeSize(); i++){
                        if(house.isGrowing(i)){
                            Optional<HHologram> hologram = HCore.findHologramByID("farm" + no + "node" + i);
                            int finalI = i;
                            hologram.ifPresent((holo) -> holo.setLines(house.getLines(finalI)));
                        }else{
                            Optional<HHologram> hologram = HCore.findHologramByID("farm" + no + "node" + i);
                            int finalI1 = i;
                            hologram.ifPresent((holo) ->{
                                if(holo.getLines().size() > 4){
                                    holo.setLines(house.getLines(finalI1));
                                }
                            });
                        }
                    }
                    if(house.remainingHours() < -10){
                       house.removeOwner();
                       Optional<HHologram> hologram = HCore.findHologramByID("farm" + no);
                       hologram.ifPresent((holo) -> holo.setLines(house.getLines()));
                    }
                }
            }
        }.runTaskTimer(Main.instance, 0, 1);
    }

    public static void removeFarmIfPlayerHas(Player player){
        for(String no : data.getData().keySet()) {
            Farm house = new Farm(no);
            if(house.hasOwner() && house.getOwner().equals(player)){
                house.removeOwner();
            }
        }
    }


}
