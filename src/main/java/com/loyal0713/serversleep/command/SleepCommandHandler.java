package com.loyal0713.serversleep.command;

import com.loyal0713.serversleep.ServerSleep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SleepCommandHandler implements CommandExecutor, TabCompleter {

    private final ServerSleep plugin;

    public SleepCommandHandler(ServerSleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Current sleep percentage: " + plugin.getSleepPercentage() + "%");
            return true;
        }

        String argument = args[0].toLowerCase();

        if (argument.equals("reload")) {
            plugin.loadSleepPercentage();
            sender.sendMessage("ServerSleep config reloaded. Sleep percentage: " + plugin.getSleepPercentage() + "%");
            return true;
        }

        if (argument.equals("percent")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /serversleep percent <0-100>");
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number: " + args[1]);
                return true;
            }
            if (value < 0 || value > 100) {
                sender.sendMessage("Value must be between 0 and 100.");
                return true;
            }
            plugin.setSleepPercentage(value);
            sender.sendMessage("Sleep percentage set to " + value + "%");
            return true;
        }

        sender.sendMessage("Usage: /serversleep [percent <0-100>] [reload]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<String>();
            for (String sub : Arrays.asList("percent", "reload")) {
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("percent")) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
