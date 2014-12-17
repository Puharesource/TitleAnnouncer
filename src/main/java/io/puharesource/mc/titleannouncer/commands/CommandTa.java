package io.puharesource.mc.titleannouncer.commands;

import io.puharesource.mc.titleannouncer.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTa implements CommandExecutor {

    private Main plugin;

    public CommandTa(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("ta")) return false;

        if (!sender.hasPermission("titleannouncer.command.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
            sender.sendMessage(ChatColor.RED + "    /ta reload");
            return true;
        }

        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "The config has been reloaded.");
        return true;
    }
}
