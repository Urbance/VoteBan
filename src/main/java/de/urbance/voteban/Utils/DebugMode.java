package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class DebugMode {
    private Main plugin;
    private FileConfiguration config;

    public DebugMode() {
        this.plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;
    }

    public void printStartVoteInformations() {
        if (!config.getBoolean("general-settings.debug-mode")) return;

        StringBuilder stringBuilder = new StringBuilder();
        String header = "\n========= DEBUG =========";

        String voteInitiator = "\nvoteInitiator: " + VoteManager.voteInitiator + "\n";
        String voteTarget = "voteTarget: " + VoteManager.voteTarget + "\n";
        String isVoteRunning = "isVoteRunning: " + plugin.isVotingRunning ;

        stringBuilder.append(header);
        stringBuilder.append(voteInitiator);
        stringBuilder.append(voteTarget);
        stringBuilder.append(isVoteRunning);
        stringBuilder.append(header);

        Bukkit.getLogger().info(stringBuilder.toString());
    }

    public void printFinishedVoteInformations(int playersNeededForBan, int intJoinedVotePlayers) {
        if (!config.getBoolean("general-settings.debug-mode")) return;

        StringBuilder stringBuilder = new StringBuilder();
        String header = "\n========= DEBUG =========";

        String playersNeeded = "\nplayersNeeded: " + playersNeededForBan + "\n";
        String joinedVotePlayers = "joinedVotePlayers: " + intJoinedVotePlayers + "\n";
        String wasVoteProbablySuccessful = "wasVoteProbablySuccessful: " +  (intJoinedVotePlayers >= playersNeededForBan);

        stringBuilder.append(header);
        stringBuilder.append(playersNeeded);
        stringBuilder.append(joinedVotePlayers);
        stringBuilder.append(wasVoteProbablySuccessful);
        stringBuilder.append(header);

        Bukkit.getLogger().info(stringBuilder.toString());
    }
}
