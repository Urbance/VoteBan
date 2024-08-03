package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VoteBanCommandCooldown {
    private static HashMap<UUID, LocalDateTime> votebanPlayerCooldownList = new HashMap<>();
    private LocalDateTime currentDate;
    private Main plugin;
    private FileConfiguration config;

    public VoteBanCommandCooldown() {
        this.plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.currentDate = LocalDateTime.parse(LocalDateTime.now().toString(), dateTimeFormatter);
    }

    public boolean addPlayer(UUID playerUUID) {
        if (playerUUID != null) {
            votebanPlayerCooldownList.put(playerUUID, getUntilDate());
            return true;
        }
        return false;
    }

    public boolean playerHasCommandCooldown(UUID playerUUID) {
        LocalDateTime cooldownUntilDate = votebanPlayerCooldownList.get(playerUUID);

        if (cooldownUntilDate == null) return false;

        return cooldownUntilDate.isAfter(this.currentDate);
    }

    public long getPlayerCommandCooldown(UUID playerUUID) {
        LocalDateTime cooldownUntilDate = votebanPlayerCooldownList.get(playerUUID);

        if (cooldownUntilDate == null) return 0;

        Duration duration = Duration.between(currentDate, cooldownUntilDate);

        return duration.toSeconds();
    }

    private LocalDateTime getUntilDate() {
        int cooldownLength = config.getInt("voteban-settings.general-settings.player-start-voteban-cooldown");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime commandCooldownDate = LocalDateTime.parse(LocalDateTime.now().toString(), dateTimeFormatter);

        return commandCooldownDate.plusSeconds(cooldownLength);
    }
}
