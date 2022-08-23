package com.chilieutenant.theringfarming.handlers;

import com.hakan.core.hologram.HHologram;
import com.hakan.core.hologram.event.HHologramClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MainListener implements Listener {

    @EventHandler
    public void onClick(HHologramClickEvent event) {
        HHologram hologram = event.getHologram();
    }

}
