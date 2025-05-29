package net.xenvision.xennotice.notice;

import net.xenvision.xennotice.XenNotice;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class NoticeStorage {
    private final File file;

    public NoticeStorage(XenNotice plugin) {
        this.file = new File(plugin.getDataFolder(), "notices.yml");
    }

    public void saveAll(Collection<Notice> notices) {
        YamlConfiguration cfg = new YamlConfiguration();
        int i = 0;
        for (Notice notice : notices) {
            String path = "notices." + i++;
            cfg.set(path + ".id", notice.getId().toString());
            cfg.set(path + ".author", notice.getAuthor());
            cfg.set(path + ".authorUuid", notice.getAuthorUuid());
            cfg.set(path + ".message", notice.getMessage());
            cfg.set(path + ".created", notice.getCreated().toString());
            cfg.set(path + ".expiry", notice.getExpiry().toString());
            cfg.set(path + ".iconMaterial", notice.getIconMaterial());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Notice> loadAll() {
        List<Notice> notices = new ArrayList<>();
        if (!file.exists()) return notices;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("notices")) return notices;
        for (String key : Objects.requireNonNull(cfg.getConfigurationSection("notices")).getKeys(false)) {
            String path = "notices." + key;
            try {
                UUID id = UUID.fromString(Objects.requireNonNull(cfg.getString(path + ".id")));
                String author = cfg.getString(path + ".author");
                String authorUuid = cfg.getString(path + ".authorUuid");
                String message = cfg.getString(path + ".message");
                LocalDateTime created = LocalDateTime.parse(Objects.requireNonNull(cfg.getString(path + ".created")));
                LocalDateTime expiry = LocalDateTime.parse(Objects.requireNonNull(cfg.getString(path + ".expiry")));
                String iconMaterial = cfg.getString(path + ".iconMaterial");
                notices.add(new Notice(id, author, authorUuid, message, created, expiry, iconMaterial));
            } catch (Exception ignored) {}
        }
        return notices;
    }
}