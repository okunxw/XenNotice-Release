package net.xenvision.xennotice.command;

import net.xenvision.xennotice.XenNotice;
import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.notice.NoticeManager;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NoticeCommand implements CommandExecutor {

    private final XenNotice plugin;
    private final NoticeManager noticeManager;
    // Для подтверждения удаления
    private final Map<UUID, UUID> pendingDelete = new HashMap<>();

    public NoticeCommand(XenNotice plugin, NoticeManager noticeManager) {
        this.plugin = plugin;
        this.noticeManager = noticeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize("&cТолько для игроков!"));
            return true;
        }

        // /notice help или без аргументов
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : plugin.getLang().getList("help_main")) {
                player.sendMessage(ColorUtil.colorize(line));
            }
            return true;
        }

        // /notice add [icon] <text>
        if (args[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("xennotice.add")) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-add-no-perm")));
                return true;
            }
            int maxNotices = plugin.getConfig().getInt("max-notices", 25);
            if (noticeManager.getNoticeCount() >= maxNotices) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("limit-reached")));
                return true;
            }
            if (args.length < 2) {
                for (String line : plugin.getLang().getList("help_main")) {
                    player.sendMessage(ColorUtil.colorize(line));
                }
                return true;
            }

            List<String> allowedIcons = plugin.getConfig().getStringList("menu.allowed-icons");
            String defaultIcon = plugin.getConfig().getString("menu.default-icon", "PAPER");
            String icon = defaultIcon;
            String message;

            if (allowedIcons.stream().anyMatch(ic -> ic.equalsIgnoreCase(args[1]))) {
                icon = args[1].toUpperCase();
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.colorize("&cУкажите текст объявления после иконки!"));
                    return true;
                }
                message = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
            } else {
                message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            }

            if (message.length() < 2) {
                player.sendMessage(ColorUtil.colorize("&cТекст объявления слишком короткий!"));
                return true;
            }

            // lifetime
            long days = 1;
            String lifetime = plugin.getConfig().getString("notice-lifetime", "1d");
            if (lifetime.endsWith("d")) {
                try {
                    days = Long.parseLong(lifetime.replace("d", ""));
                } catch (Exception ignored) {}
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = now.plusDays(days);

            Notice notice = new Notice(
                    UUID.randomUUID(),
                    player.getName(),
                    player.getUniqueId().toString(),
                    message,
                    now,
                    expiry,
                    icon
            );

            noticeManager.addNotice(notice);
            player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-add-success")));

            String expiryFormat = plugin.getLang().get("notice-lifetime-format");
            if (expiryFormat != null && !expiryFormat.isEmpty()) {
                String dateString = expiry.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                player.sendMessage(ColorUtil.colorize(expiryFormat.replace("%expiry%", dateString)));
            }
            return true;
        }

        // /notice remove <id>
        if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
            UUID noticeId;
            try {
                noticeId = UUID.fromString(args[1]);
            } catch (Exception ex) {
                player.sendMessage(ColorUtil.colorize("&cНекорректный ID!"));
                return true;
            }
            Optional<Notice> opt = noticeManager.getNotice(noticeId);
            if (opt.isEmpty()) {
                player.sendMessage(ColorUtil.colorize("&cОбъявление не найдено!"));
                return true;
            }
            Notice notice = opt.get();
            boolean isOwn = player.getUniqueId().toString().equals(notice.getAuthorUuid());
            if (isOwn) {
                if (!player.hasPermission("xennotice.remove")) {
                    player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-denied")));
                    return true;
                }
            } else {
                if (!player.hasPermission("xennotice.remove.others")) {
                    player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-other-denied")));
                    return true;
                }
            }
            // Подтверждение
            pendingDelete.put(player.getUniqueId(), noticeId);
            for (String line : plugin.getLang().getList("notice-delete-confirm")) {
                player.sendMessage(ColorUtil.colorize(line));
            }
            return true;
        }

        // /notice confirm
        if (args[0].equalsIgnoreCase("confirm")) {
            UUID pending = pendingDelete.get(player.getUniqueId());
            if (pending == null) {
                player.sendMessage(ColorUtil.colorize("&cНет ожидающего удаления объявления!"));
                return true;
            }
            Optional<Notice> opt = noticeManager.getNotice(pending);
            if (opt.isEmpty()) {
                player.sendMessage(ColorUtil.colorize("&cОбъявление уже удалено!"));
                pendingDelete.remove(player.getUniqueId());
                return true;
            }
            Notice notice = opt.get();
            boolean isOwn = player.getUniqueId().toString().equals(notice.getAuthorUuid());
            if (isOwn) {
                if (!player.hasPermission("xennotice.remove")) {
                    player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-denied")));
                    return true;
                }
            } else {
                if (!player.hasPermission("xennotice.remove.others")) {
                    player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-other-denied")));
                    return true;
                }
            }
            noticeManager.removeNotice(pending);
            player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-success")));
            pendingDelete.remove(player.getUniqueId());
            return true;
        }

        // /notice cancel
        if (args[0].equalsIgnoreCase("cancel")) {
            if (pendingDelete.remove(player.getUniqueId()) != null) {
                player.sendMessage(ColorUtil.colorize("&aУдаление отменено."));
            } else {
                player.sendMessage(ColorUtil.colorize("&cНет ожидающего удаления объявления!"));
            }
            return true;
        }

        // неизвестная команда
        for (String line : plugin.getLang().getList("help_main")) {
            player.sendMessage(ColorUtil.colorize(line));
        }
        return true;
    }
}