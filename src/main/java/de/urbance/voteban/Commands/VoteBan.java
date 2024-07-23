package de.urbance.voteban.Commands;

import de.urbance.voteban.Main;
import de.urbance.voteban.Utils.Immunity;
import de.urbance.voteban.Utils.Placeholders;
import de.urbance.voteban.Utils.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteBan implements CommandExecutor, TabCompleter {
    private Main plugin;
    private String prefix;
    private FileConfiguration messagesConfig;
    private FileConfiguration config;
    private VoteManager voteManager = new VoteManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;
        this.prefix = plugin.prefix;
        this.messagesConfig = plugin.messagesConfig;

        performAction(sender, args);

        return false;
    }

    private void performAction(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You can't execute this command as console!"));
            return;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return;
        }

        switch (args[0]) {
            case "join":
                joinVoteBan(sender);
                break;
            case "start":
                startVoting(sender, args);
                break;
            default:
                sendHelpMessage(sender);
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        if (sender.hasPermission("voteban.player.*") || sender.hasPermission("voteban.player.help")) {
            List<String> helpMessageList = messagesConfig.getStringList("help");
            StringBuilder helpMessage = new StringBuilder();

            for (String line : helpMessageList) {
                if (helpMessage.length() == 0) {
                    helpMessage = new StringBuilder(line);
                    continue;
                }
                helpMessage.append("\n").append(line);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(helpMessage)));
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.no-permission")));
    }

    private void joinVoteBan(CommandSender sender) {
        if (sender.hasPermission("voteban.player.join") || sender.hasPermission("voteban.player.*")) {
            if (!plugin.isVotingRunning) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "There isn't a running voting!"));
                return;
            }
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You can't take part in the voting as a console."));
                return;
            }
            if (VoteManager.voteInitiator == ((Player) sender).getUniqueId()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You already joined the voting."));
                return;
            }
            if (VoteManager.voteTarget == ((Player) sender).getUniqueId()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You can't join the voting."));
                return;
            }

            voteManager.addJoin();
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The player &e" + sender.getName() + " &7joined the voteban!"));
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.no-permission")));
    }

    private void startVoting(CommandSender sender, String[] args) {
        if (!validateStartVotingSender(sender, args)) return;
        if (!validateStartVotingCommandInput(sender, args)) return;
        if (!validateMaximumOnlineTeamMembers(sender)) return;

        Player targetplayer = Bukkit.getPlayer(args[1]);

        if (targetplayer.hasPermission("voteban.administration.*") || targetplayer.hasPermission("voteban.miscellaneous.bypass")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.start-voting-against-the-player")));
            return;
        }
        if (validateTargetPlayerImmunity(targetplayer)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.player-is-immune")));
            return;
        }

        voteManager.initVoteBan(sender, targetplayer);
   }

    private boolean validateStartVotingSender(CommandSender sender, String[] args) {
       if (!sender.hasPermission("voteban.player.start") && !sender.hasPermission("voteban.player.*")) {
           sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.no-permission")));
           return false;
       }
       if (args.length == 1) {
           sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.specify-player")));
           return false;
       }
       if (args.length > 2) {
           sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.wrong-usage")));
           return false;
       }
       if (!validateVoteInitiatorPlaytime(sender)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.not-enough-playtime")));
            return false;
       }
        return true;
   }

    private boolean validateStartVotingCommandInput(CommandSender sender, String[] args) {
        if (sender instanceof Player && sender.getName().equals(args[1])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.start-voting-against-yourself")));
            return false;
        }
        if (Bukkit.getPlayer(args[1]) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.player-not-online")));
            return false;
        }
        if (Bukkit.getOnlinePlayers().size() < config.getInt("voteban-settings.start-voting-conditions.minimum-online-players")) {
            Map<String, String> values = new HashMap<>();
            values.put("voteban_minimum_players", config.getString("voteban-settings.start-voting-conditions.minimum-online-players"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + new Placeholders(messagesConfig.getString("errors.not-enough-players-online"), values).set()));
            return false;
        }

        return true;
    }

    private boolean validateVoteInitiatorPlaytime(CommandSender sender) {
        Player voteInitiator = (Player) sender;
        long voteInitiatorPlaytimeInSeconds = voteInitiator.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        long requiredPlaytimeInSeconds = config.getLong("voteban-settings.start-voting-conditions.required-playtime");

        return voteInitiatorPlaytimeInSeconds >= requiredPlaytimeInSeconds;
    }

    private boolean validateMaximumOnlineTeamMembers(CommandSender sender) {
        int onlineTeamMembers = 0;
        int maximumOnlineTeamMembers = config.getInt("voteban-settings.start-voting-conditions.maximum-online-team-members");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("voteban.administration.teammember")) onlineTeamMembers += 1;
        }

        if (onlineTeamMembers == 0 && maximumOnlineTeamMembers == 0) return true;

        if (onlineTeamMembers > maximumOnlineTeamMembers) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.too-many-online-team-members")));
            return false;
        }

        return true;
    }

    private boolean validateTargetPlayerImmunity(Player targetPlayer) {
        Immunity immunity = new Immunity(targetPlayer);
        return immunity.hasImmunity();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tabCompletions = new ArrayList<>();

        switch(args.length) {
            case 1 -> {
                if (sender.hasPermission("voteban.player.start") || sender.hasPermission("voteban.player.*")) tabCompletions.add("start");
                if (sender.hasPermission("voteban.player.join") || sender.hasPermission("voteban.player.*")) tabCompletions.add("join");
            }
        }

        return tabCompletions;
    }
}
