package de.urbance.voteban.Utils;

import de.urbance.voteban.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileManager {
    public String fileName;
    public FileConfiguration fileConfiguration;
    public Main plugin;
    public File file;

    public FileManager(String fileName, Main plugin) {
        this.fileName = fileName;
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        if (this.fileConfiguration == null)
            reload();
        return this.fileConfiguration;
    }

    public void reload() {
        if (this.fileConfiguration == null)
            this.file = new File(this.plugin.getDataFolder(), fileName);
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultStream = this.plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.fileConfiguration.setDefaults(defaultConfig);
        }

        switch (fileName) {
            case "config.yml":
                plugin.config = fileConfiguration;
                plugin.prefix = fileConfiguration.getString("general-settings.prefix");
                break;
            case "messages.yml":
                plugin.messagesConfig = fileConfiguration;
                break;
            case "immunity.yml":
                plugin.immunityData = fileConfiguration;
                break;
        }
    }

    public void save() {
        if (this.file == null)
            saveDefaultConfig();
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDefaultConfig() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder(), fileName);

        if (!this.file.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }
}
