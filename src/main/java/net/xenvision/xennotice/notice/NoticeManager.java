package net.xenvision.xennotice.notice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.Iterator;

public class NoticeManager {
    private final Map<UUID, Notice> notices = new ConcurrentHashMap<>();
    private NoticeStorage storage;

    public void setStorage(NoticeStorage storage) {
        this.storage = storage;
    }

    public void addNotice(Notice notice) {
        notices.put(notice.getId(), notice);
        save();
    }

    public void removeNotice(UUID id) {
        notices.remove(id);
        save();
    }

    public List<Notice> getAllNotices() {
        return new ArrayList<>(notices.values());
    }

    public Optional<Notice> getNotice(UUID id) {
        return Optional.ofNullable(notices.get(id));
    }

    public void removeExpired() {
        List<UUID> toRemove = notices.values().stream()
                .filter(Notice::isExpired)
                .map(Notice::getId)
                .toList();
        toRemove.forEach(notices::remove);
        save();
    }

    public int getNoticeCount() {
        return notices.size();
    }

    public void clear() {
        notices.clear();
        save();
    }

    public void save() {
        if (storage != null) storage.saveAll(notices.values());
    }

    public int cleanupExpiredNotices() {
        int removed = 0;
        Iterator<Notice> iter = notices.values().iterator();
        LocalDateTime now = LocalDateTime.now();
        while (iter.hasNext()) {
            Notice notice = iter.next();
            if (notice.getExpiry().isBefore(now)) {
                iter.remove();
                removed++;
            }
        }
        if (removed > 0) saveNotices();
        return removed;
    }

    public void load() {
        if (storage != null) {
            notices.clear();
            for (Notice notice : storage.loadAll()) {
                notices.put(notice.getId(), notice);
            }
        }
    }
}