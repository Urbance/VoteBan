package de.urbance.voteban;

import de.urbance.voteban.Commands.Vba;
import de.urbance.voteban.Commands.VoteBan;
import de.urbance.voteban.Utils.FileManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
   public FileConfiguration config;
   public String prefix;
   public FileConfiguration messagesConfig;
   public FileConfiguration immunityData;
   public boolean isVotingRunning;

    @Override
    public void onEnable() {
        setupConfigs();
        bStats();

        this.prefix = config.getString("general-settings.prefix");

        getLogger().info("VoteBan is now ready");
        getCommand("voteban").setExecutor(new VoteBan());
        getCommand("voteban").setTabCompleter(new VoteBan());
        getCommand("vba").setExecutor(new Vba());
        getCommand("vba").setTabCompleter(new Vba());
    }

    @Override
    public void onDisable() {
    }

    private void setupConfigs() {
        // config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();
        this.config = getConfig();

        // messages.yml
        FileManager messagesFileManager = new FileManager("messages.yml", this);
        this.messagesConfig = messagesFileManager.getConfig();
        messagesConfig.options().copyDefaults(true);
        messagesFileManager.save();

        // immunity.yml
        FileManager immunityFileManager = new FileManager("immunity.yml", this);
        this.immunityData = immunityFileManager.getConfig();
        immunityData.options().copyDefaults(true);
        immunityFileManager.save();
    }

    private void bStats() {
        int pluginId = 22280;
        Metrics metrics = new Metrics(this, pluginId);
    }
}
