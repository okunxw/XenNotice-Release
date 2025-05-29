package net.xenvision.xennotice.util;

import org.bukkit.configuration.file.FileConfiguration;
import net.xenvision.xennotice.XenNotice;

public class ConfigUtil {
    private static FileConfiguration config() {
        return XenNotice.getInstance().getConfig();
    }

    public static String getString(String path, String def) {
        return config().getString(path, def);
    }

    public static int getInt(String path, int def) {
        return config().getInt(path, def);
    }

    public static long getLong(String path, long def) {
        return config().getLong(path, def);
    }

    public static boolean getBoolean(String path, boolean def) {
        return config().getBoolean(path, def);
    }
}
