package htpvplog;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        // 1. Log: Enabling (Green)
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "========================================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  HT-PVPLog v" + getDescription().getVersion() + " by mustafa8907");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  Status: ACTIVATED SUCCESSFULLY");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "========================================");

        // Config ve Dil Dosyaları
        saveDefaultConfig();
        prepareLanguageFiles();
        loadLanguage();

        // Manager ve Eventler
        this.combatManager = new CombatManager(this);
        getServer().getPluginManager().registerEvents(new EventListeners(this), this);
    }

    @Override
    public void onDisable() {
        if (combatManager != null) {
            combatManager.cleanup();
        }

        // 2. Log: Disabling (Red)
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "========================================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  HT-PVPLog v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  Status: DEACTIVATED / DISABLING");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "========================================");
    }

    private void prepareLanguageFiles() {
        String[] languages = {"en", "ru", "ar", "es", "pt", "ja", "de", "fr"};
        File langFolder = new File(getDataFolder(), "language");
        if (!langFolder.exists()) langFolder.mkdirs();

        for (String lang : languages) {
            String fileName = "language/messages_" + lang + ".yml";
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                saveResource(fileName, false);
            }
        }
    }

    public void loadLanguage() {
        String langFileName = getConfig().getString("language-file", "messages_en.yml");
        File langFile = new File(getDataFolder(), "language/" + langFileName);

        if (!langFile.exists()) {
            langFile = new File(getDataFolder(), "language/messages_en.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String path) {
        if (langConfig == null) return path;
        String msg = langConfig.getString(path);
        return msg != null ? ColorUtils.colorize(msg) : path;
    }

    public static HTPVPLog getInstance() { return instance; }
    public CombatManager getCombatManager() { return combatManager; }
}
