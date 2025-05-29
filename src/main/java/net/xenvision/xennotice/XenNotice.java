package net.xenvision.xennotice;

import net.xenvision.xennotice.util.Lang;
import net.xenvision.xennotice.notice.NoticeStorage;
import net.xenvision.xennotice.command.NoticeCommand;
import net.xenvision.xennotice.gui.NoticeMenu;
import net.xenvision.xennotice.listener.MenuListener;
import net.xenvision.xennotice.notice.NoticeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XenNotice extends JavaPlugin {

    private static XenNotice instance;
    private NoticeManager noticeManager;
    private NoticeMenu noticeMenu;
    private Lang lang;

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

        getLogger().info("XenNotice enabled!");
    }

    @Override
    public void onDisable() {
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
}
