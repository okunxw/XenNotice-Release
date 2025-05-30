package net.xenvision.xennotice;

import net.xenvision.xennotice.util.Lang;
import net.xenvision.xennotice.notice.NoticeStorage;
import net.xenvision.xennotice.command.NoticeCommand;
import net.xenvision.xennotice.gui.NoticeMenu;
import net.xenvision.xennotice.listener.MenuListener;
import net.xenvision.xennotice.notice.NoticeManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class XenNotice extends JavaPlugin {

    private static XenNotice instance;
    private NoticeManager noticeManager;
    private NoticeMenu noticeMenu;
    private Lang lang;
    private int cleanupTaskId = -1;

    @Override
    public void onEnable() {
        instance = this;
        lang = new Lang(this, "ru_RU.yml");
        this.noticeManager = new NoticeManager();

        // 1. Создаём NoticeStorage
        NoticeStorage storage = new NoticeStorage(this);

        // 2. Передаём его менеджеру
        this.noticeManager.setStorage(storage);

        // 3. Загружаем объявления
        this.noticeManager.load();

        // 4. Создаём меню
        this.noticeMenu = new NoticeMenu(this, noticeManager);

        // 5. Регистрируем команду и listener
        this.getCommand("notice").setExecutor(new NoticeCommand(this, noticeManager));
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // 6. Запуск автоочистки
        startCleanupTask();

        getLogger().info("XenNotice enabled!");
    }

    @Override
    public void onDisable() {
        if (cleanupTaskId != -1) Bukkit.getScheduler().cancelTask(cleanupTaskId);
        if (noticeManager != null) noticeManager.save();
        getLogger().info("XenNotice disabled!");
    }

    public static XenNotice getInstance() {
        return instance;
    }

    public NoticeManager getNoticeManager() {
        return noticeManager;
    }

    public NoticeMenu getNoticeMenu() {
        return noticeMenu;
    }

    public Lang getLang() {
        return lang;
    }

    // --- Автоочистка объявлений ---
    public void startCleanupTask() {
        if (!getConfig().getBoolean("auto-cleanup", true)) return;
        String intervalStr = getConfig().getString("cleanup-interval", "1h");
        long ticks = parseIntervalToTicks(intervalStr);
        if (ticks < 20) ticks = 20 * 60 * 60; // минимум 1 час
        cleanupTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            int removed = noticeManager.cleanupExpiredNotices();
            if (removed > 0) getLogger().info("Auto-cleanup: removed " + removed + " expired notices.");
        }, ticks, ticks).getTaskId();
    }

    private long parseIntervalToTicks(String s) {
        try {
            s = s.trim().toLowerCase();
            if (s.endsWith("h")) return Long.parseLong(s.replace("h", "")) * 20 * 60 * 60;
            if (s.endsWith("m")) return Long.parseLong(s.replace("m", "")) * 20 * 60;
            if (s.endsWith("s")) return Long.parseLong(s.replace("s", "")) * 20;
            return Long.parseLong(s) * 20; // секунды по умолчанию
        } catch (Exception e) {
            return 20 * 60 * 60; // 1 час если ошиблись
        }
    }
}