package com.loyal0713.serversleep;

import com.loyal0713.serversleep.command.SleepCommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerSleep extends JavaPlugin {

    private int sleepPercentage;
    private String skipMessage;
    private boolean clearWeather;
    private boolean resetPhantoms;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getServer().getPluginManager().registerEvents(new SleepListener(this), this);

        SleepCommandHandler handler = new SleepCommandHandler(this);
        getCommand("serversleep").setExecutor(handler);
        getCommand("serversleep").setTabCompleter(handler);

        getLogger().info("ServerSleep enabled. Sleep percentage: " + sleepPercentage + "%");
    }

    public void loadConfig() {
        reloadConfig();

        sleepPercentage = getConfig().getInt("sleep-percentage", 100);
        if (sleepPercentage < 0 || sleepPercentage > 100) {
            getLogger().warning("sleep-percentage must be 0-100, defaulting to 100.");
            sleepPercentage = 100;
        }

        skipMessage   = getConfig().getString("skip-message", "");
        clearWeather  = getConfig().getBoolean("clear-weather", true);
        resetPhantoms = getConfig().getBoolean("reset-phantoms", true);
    }

    public void setSleepPercentage(int percentage) {
        sleepPercentage = percentage;
        getConfig().set("sleep-percentage", percentage);
        saveConfig();
    }

    public int getSleepPercentage() { return sleepPercentage; }

    public void checkSleep(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        long time = world.getTime();
        if (time < 12542 || time > 23460) return;

        int eligible = 0;
        int sleeping = 0;
        for (Player player : world.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            eligible++;
            if (player.isSleeping()) sleeping++;
        }

        if (eligible == 0) return;

        int required = Math.max(1, (int) Math.ceil(eligible * sleepPercentage / 100.0));
        if (sleeping >= required) skipNight(world);
    }

    private void skipNight(World world) {
        if (Boolean.TRUE.equals(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))) {
            if (resetPhantoms) resetPhantomTimers(world);
            world.setTime((world.getFullTime() / 24000 + 1) * 24000);
        }
        if (clearWeather && Boolean.TRUE.equals(world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))) {
            world.setStorm(false);
            world.setThundering(false);
        }
        if (skipMessage != null && !skipMessage.isEmpty()) {
            String colored = ChatColor.translateAlternateColorCodes('&', skipMessage);
            for (Player player : world.getPlayers()) {
                player.sendMessage(colored);
            }
        }
    }

    // TIME_SINCE_REST was added with phantoms in 1.13; safe to ignore on older versions
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
