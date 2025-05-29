package net.xenvision.xennotice.notice;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notice {
    private final UUID id;
    private final String author;
    private final String authorUuid;
    private final String message;
    private final LocalDateTime created;
    private final LocalDateTime expiry;
    private final String iconMaterial;

    public Notice(UUID id, String author, String authorUuid, String message, LocalDateTime created, LocalDateTime expiry, String iconMaterial) {
        this.id = id;
        this.author = author;
        this.authorUuid = authorUuid;
        this.message = message;
        this.created = created;
        this.expiry = expiry;
        this.iconMaterial = iconMaterial;
    }

    public UUID getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorUuid() {
        return authorUuid;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public boolean isExpired() {
        return expiry != null && LocalDateTime.now().isAfter(expiry);
    }
}