package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteManager {
    public static UUID voteInitiator;
    public static UUID voteTarget;
    public Player voteTargetPlayer;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private Main plugin;
    private String prefix;
    private int voteJoinedPlayers;
    private int playersNeededForBan;

    public VoteManager() {
        this.plugin = Main.getPlugin(Main.class);
        this.prefix = plugin.prefix;
        this.config = plugin.config;
        this.messagesConfig = plugin.messagesConfig;
        this.voteJoinedPlayers = 0;
    }

    public void initVoteBan(CommandSender sender, Player voteTargetPlayer) {
        if (!checkConditionsForNewVoting(sender)) {
            return;
        }

        voteInitiator = ((Player) sender).getUniqueId();
        voteTarget = voteTargetPlayer.getUniqueId();
        plugin.isVotingRunning = true;
        this.voteTargetPlayer = voteTargetPlayer;

        new DebugMode().printStartVoteInformations();

        determineNeededPlayersForSuccessfulVoteBan();
        startVoteBan(sender);
    }

    private boolean checkConditionsForNewVoting(CommandSender sender) {
        if (!isPercentageRequiredValueIsValid()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred. Please contact the administrator of the server."));
            plugin.getLogger().warning("An error has occured - The value 'voteban.ban-conditions.required-joined-ban-players-percentage' in the config.yml must be an integer between 0 to 100!");
            return false;
        }
        if (plugin.isVotingRunning) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.already-a-running-voting")));
            return false;
        }

        return true;
    }

    private void determineNeededPlayersForSuccessfulVoteBan() {
        double percentageJoinedPlayersNeededForBan = config.getInt("voteban-settings.ban-conditions.required-joined-ban-players-percentage");
        double doublePlayersNeededForBan = percentageJoinedPlayersNeededForBan * Bukkit.getOnlinePlayers().size() / 100;
        this.playersNeededForBan = (int) Math.ceil(doublePlayersNeededForBan);
    }

    private void startVoteBan(CommandSender sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(voteInitiator)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
                        replacePlaceholders(messagesConfig.getString("voting.start-initiator"), sender, voteTargetPlayer)));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +
                        replacePlaceholders(messagesConfig.getString("voting.start"), sender, voteTargetPlayer)));
            }
        }
        addJoin();
        startVotingCountdown();
    }

    private void startVotingCountdown() {;
        new BukkitRunnable() {
            final int duration = config.getInt("voteban-settings.ban-settings.vote-cooldown");
            int remainingSeconds = duration;


            @Override
            public void run() {
                if (remainingSeconds == duration / 2) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix +
                            getRemainingVotingTimeMessage(voteTargetPlayer, remainingSeconds)));
                }
                if (remainingSeconds == 0) {
                    banPlayer(voteTargetPlayer);
                    new DebugMode().printFinishedVoteInformations(playersNeededForBan, voteJoinedPlayers);
                    resetValues();
                    this.cancel();
                }
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void banPlayer(Player targetPlayer) {
        if (voteJoinedPlayers < playersNeededForBan) {
            Immunity immunity = new Immunity(targetPlayer);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime immunityDate = LocalDateTime.parse(LocalDateTime.now().toString(), dateTimeFormatter);
            immunityDate = immunityDate.plusSeconds(config.getInt("voteban-settings.failed-voteban-settings.immunity-length-target-player"));
            immunity.addPlayer(immunityDate);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + messagesConfig.getString("errors.too-few-players-have-voted")));
            return;
        }

        BanManager banList = new BanManager(targetPlayer);
        banList.addPlayer();

        Map<String, String> values = new HashMap<>();
        values.put("voteban_targetplayer_name", targetPlayer.getName());

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + new Placeholders(messagesConfig.getString("voting.player-was-banned"), values).set()));
    }

    private void resetValues() {
        plugin.isVotingRunning = false;
        voteJoinedPlayers = 0;
        voteInitiator = null;
        voteTarget = null;
    }

    public void addJoin() {
        if (!plugin.isVotingRunning) {
            return;
        }
        voteJoinedPlayers += 1;
    }

    private boolean isPercentageRequiredValueIsValid() {
        String percentageRequired = config.getString("voteban-settings.ban-conditions.required-joined-ban-players-percentage");
        try {
            int intPercentageRequired = Integer.parseInt(percentageRequired);
            return intPercentageRequired >= 0 && intPercentageRequired <= 100;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String replacePlaceholders(String message, CommandSender sender, Player targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("voteban_player_name", sender.getName());
        values.put("voteban_targetplayer_name", targetPlayer.getName());
        values.put("voteban_players_needed", String.valueOf(playersNeededForBan));

        return new Placeholders(message, values).set();
    }

    private String getRemainingVotingTimeMessage(Player targetPlayer, int remainingSeconds) {
        Map<String, String> values = new HashMap<>();
        values.put("voteban_targetplayer_name", targetPlayer.getName());
        values.put("voteban_time_left", String.valueOf(remainingSeconds));

        return new Placeholders(messagesConfig.getString("voting.time-left"), values).set();
    }
}