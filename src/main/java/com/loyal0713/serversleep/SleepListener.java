package com.loyal0713.serversleep;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SleepListener implements Listener {

    private final ServerSleep plugin;

    public SleepListener(ServerSleep plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        final World world = event.getPlayer().getWorld();
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.checkSleep(world);
            }
        }, 100L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!player.isSleeping()) {
            return;
        }
        final World world = player.getWorld();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.checkSleep(world);
            }
        });
    }
}
