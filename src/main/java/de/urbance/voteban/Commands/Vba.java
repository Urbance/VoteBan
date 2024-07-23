package de.urbance.voteban.Commands;

import de.urbance.voteban.Main;
import de.urbance.voteban.Utils.FileManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.List;

public class Vba implements CommandExecutor, TabCompleter {
    private Main plugin;
    private String prefix;
    private CommandSender sender;
    private String[] args;
    private FileConfiguration messagesConfig;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin = Main.getPlugin(Main.class);
        this.prefix = plugin.prefix;
        this.sender = sender;
        this.args = args;
        this.messagesConfig = plugin.messagesConfig;

        performActions();

        return false;
    }

    private void performActions() {
        if (args.length != 1) {
            sendHelpMessage();
            return;
        }

        switch(args[0]) {
            case "version" -> sendVersion();
            case "reload" -> reloadConfigs();
            default -> sendHelpMessage();
        }
    }

    private void sendHelpMessage() {
        if (sender.hasPermission("voteban.administration.help") || sender.hasPermission("voteban.administration.*")) {
            StringBuilder helpMessage = new StringBuilder();

            helpMessage.append("&7====== &c&lVoteBan Administration Commands &7======\n");
            helpMessage.append(" \n");
            helpMessage.append("&7/vba\n");
            helpMessage.append("&7/vba version\n");
            helpMessage.append("&7/vba reload\n");

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMessage.toString()));
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.no-permission")));
    }

    private void sendVersion() {
        if (sender.hasPermission("voteban.administration.*") || sender.hasPermission("voteban.administration.version")) {
            PluginDescriptionFile pluginDescriptionFile = plugin.getDescription();

            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + "The plugin is running on version &c" + pluginDescriptionFile.getVersion()));
        }
    }

    private void reloadConfigs() {
        if (sender.hasPermission("voteban.administration.*") || sender.hasPermission("voteban.administration.reload")) {
            new FileManager("config.yml", plugin).reload();
            new FileManager("messages.yml", plugin).reload();
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.prefix + "&7Configurations successfully reloaded"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tabCompletions = new ArrayList<>();

        switch(args.length) {
            case 1 -> {
                if (sender.hasPermission("voteban.administration.version") || sender.hasPermission("voteban.administration.*")) tabCompletions.add("version");
                if (sender.hasPermission("voteban.administration.reload") || sender.hasPermission("voteban.administration.*")) tabCompletions.add("reload");
            }
        }

        return tabCompletions;
    }
}
