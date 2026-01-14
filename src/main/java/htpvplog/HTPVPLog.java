package htpvplog;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HTPVPLog extends JavaPlugin {

    private static HTPVPLog instance;
    private CombatManager combatManager;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Config Yükle
        saveDefaultConfig();
        loadLanguage();

        // Manager Başlat
        this.combatManager = new CombatManager(this);

        // Listener Kayıt
        getServer().getPluginManager().registerEvents(new EventListeners(this), this);

        getLogger().info("HT-PVPLog (mustafa8907) başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) {
            combatManager.cleanup();
        }
    }

    public static HTPVPLog getInstance() {
        return instance;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public void loadLanguage() {
        File langFolder = new File(getDataFolder(), "language");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        String langFileName = getConfig().getString("language-file", "messages_en.yml");
        File langFile = new File(langFolder, langFileName);

        if (!langFile.exists()) {
            saveResource("language/" + langFileName, false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String path) {
        if (langConfig == null) return path;
        String msg = langConfig.getString(path);
        return msg != null ? msg : path;
    }
    }
