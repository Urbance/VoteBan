package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Immunity {
    private FileManager immunityDataFileManager;
    private FileConfiguration immunityData;
    private Player player;
    private String immunityPlayerBasePath;
    private String immunityUntilPath;

    public Immunity(Player player) {
        this.immunityDataFileManager = new FileManager("immunity.yml", Main.getPlugin(Main.class));
        this.immunityData = immunityDataFileManager.getConfig();
        this.player = player;
        this.immunityPlayerBasePath = "players." + player.getUniqueId();
        this.immunityUntilPath = immunityPlayerBasePath + ".until";
    }

    public boolean addPlayer(LocalDateTime date) {
        // TODO Add option for immunity length

        try {
            immunityData.set(immunityUntilPath, date.toString());
            immunityDataFileManager.save();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String getUntilDate() {
        String immunityPlayerDate = immunityData.getString(immunityUntilPath);

        if (immunityPlayerDate == null) return "";

        return immunityPlayerDate;
    }

    public boolean hasImmunity() {
        String untilDate = getUntilDate();

        if (untilDate.isEmpty()) {
            return false;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime immunityPlayerDate = LocalDateTime.parse(getUntilDate());
        LocalDateTime currentDate = LocalDateTime.parse(LocalDateTime.now().toString(), dateTimeFormatter);

        return immunityPlayerDate.isAfter(currentDate);
    }
}
