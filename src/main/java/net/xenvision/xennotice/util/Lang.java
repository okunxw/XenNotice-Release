package net.xenvision.xennotice.util;

import net.xenvision.xennotice.XenNotice;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class Lang {

    private final YamlConfiguration config;

    public Lang(XenNotice plugin, String fileName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + fileName);
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            try (InputStream in = plugin.getResource("lang/" + fileName)) {
                if (in != null) {
                    Files.copy(in, langFile.toPath());
                }
            } catch (Exception ignored) {}
        }
        this.config = YamlConfiguration.loadConfiguration(langFile);
    }

    public String get(String key) {
        return config.getString(key, "&c[Ошибка локализации]: " + key);
    }
}