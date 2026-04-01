package com.loyal0713.serversleep;

import com.loyal0713.serversleep.command.SleepCommandHandler;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerSleep extends JavaPlugin {

    private int sleepPercentage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSleepPercentage();

        getServer().getPluginManager().registerEvents(new SleepListener(this), this);

        SleepCommandHandler handler = new SleepCommandHandler(this);
        getCommand("serversleep").setExecutor(handler);
        getCommand("serversleep").setTabCompleter(handler);

        getLogger().info("ServerSleep enabled. Sleep percentage: " + sleepPercentage + "%");
    }

    public void loadSleepPercentage() {
        reloadConfig();
        sleepPercentage = getConfig().getInt("sleep-percentage", 100);
        if (sleepPercentage < 0 || sleepPercentage > 100) {
            getLogger().warning("sleep-percentage must be 0-100, defaulting to 100.");
            sleepPercentage = 100;
        }
    }

    public void setSleepPercentage(int percentage) {
        sleepPercentage = percentage;
        getConfig().set("sleep-percentage", percentage);
        saveConfig();
    }

    public int getSleepPercentage() {
        return sleepPercentage;
    }

    public void checkSleep(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        long time = world.getTime();
        if (time < 12542 || time > 23460) {
            return;
        }

        List<Player> players = world.getPlayers();
        int eligible = 0;
        int sleeping = 0;
        for (Player player : players) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            eligible++;
            if (player.isSleeping()) {
                sleeping++;
            }
        }

        if (eligible == 0) {
            return;
        }

        int required = Math.max(1, (int) Math.ceil(eligible * sleepPercentage / 100.0));
        if (sleeping >= required) {
            skipNight(world);
        }
    }

    private void skipNight(World world) {
        
        if ("true".equals(world.getGameRuleValue("doDaylightCycle"))) {
            resetPhantomTimers(world); // doing first just to ensure timers get reset
            
            long fullTime = world.getFullTime();
            world.setTime((fullTime / 24000 + 1) * 24000);
        }
        if ("true".equals(world.getGameRuleValue("doWeatherCycle"))) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }

    // TIME_SINCE_REST was added with phantoms in 1.13; safe to ignore on 1.12
    private void resetPhantomTimers(World world) {
        Statistic timeSinceRest;
        try {
            timeSinceRest = Statistic.valueOf("TIME_SINCE_REST");
        } catch (IllegalArgumentException e) {
            return;
        }
        for (Player player : world.getPlayers()) {
            if (player.isSleeping()) {
                player.setStatistic(timeSinceRest, 0);
            }
        }
    }
}
