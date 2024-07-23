package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanManager {
    private final Player player;
    private final FileConfiguration config;

    public BanManager(Player player) {
        this.player = player;
        Main plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;
    }

    public void addPlayer() {
        long banDuration = config.getLong("voteban-settings.ban-settings.ban-duration") * 1000;
        Date banStartDate = new Date(System.currentTimeMillis());
        Date banExpiringDate = new Date(System.currentTimeMillis() + banDuration);
        String banReason = ChatColor.translateAlternateColorCodes('&', getBanReason(banStartDate, banExpiringDate));

        player.kickPlayer(banReason);
        player.getServer().getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), ChatColor.translateAlternateColorCodes('&', banReason), banExpiringDate, "VoteBan");
    }

    private String getBanReason(Date banStartDate, Date expiringDate) {
        List<String> banReasonList = config.getStringList("voteban-settings.ban-settings.ban-reason");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(config.getString("general-settings.date-format"));
        StringBuilder stringBuilder = new StringBuilder();
        String banReason = "";

        for (String reason : banReasonList) {
            banReason = String.valueOf(stringBuilder.append(reason).append("\n"));
        }

        Map<String, String> values = new HashMap<>();
        values.put("voteban_ban_start_date", simpleDateFormat.format(banStartDate));
        values.put("voteban_ban_expiring_date", simpleDateFormat.format(expiringDate));

        return new Placeholders(banReason, values).set();
    }
}

