package de.urbance.voteban;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
   public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        getLogger().info("VoteBan is now ready");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
