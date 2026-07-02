package com.vitanet.model;

import com.vitanet.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Represents an in-app notification sent to a VitaNet user.
 *
 * <p>Maps to the Mongoose {@code Notification} schema.</p>
 */
public class Notification {

    private String           notificationId;
    private String           userId;
    private NotificationType type;
    private String           message;
    private String           postId;
    private String           postType;   // "donation" or "request"
    private boolean          isRead;
    private LocalDateTime    createdAt;

    // ── Constructors ──────────────────────────────────────────

    public Notification() {
        this.isRead    = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String notificationId, String userId,
                        NotificationType type, String message,
                        String postId, String postType) {
        this.notificationId = notificationId;
        this.userId         = userId;
        this.type           = type;
        this.message        = message;
        this.postId         = postId;
        this.postType       = postType;
        this.isRead         = false;
        this.createdAt      = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String           getNotificationId() { return notificationId; }
    public void             setNotificationId(String id) { this.notificationId = id; }

    public String           getUserId()         { return userId; }
    public void             setUserId(String id){ this.userId = id; }

    public NotificationType getType()           { return type; }
    public void             setType(NotificationType t) { this.type = t; }

    public String           getMessage()        { return message; }
    public void             setMessage(String m){ this.message = m; }

    public String           getPostId()         { return postId; }
    public void             setPostId(String id){ this.postId = id; }

    public String           getPostType()       { return postType; }
    public void             setPostType(String t) { this.postType = t; }

    public boolean          isRead()            { return isRead; }

    public LocalDateTime    getCreatedAt()      { return createdAt; }

    /**
     * Marks this notification as read.
     *
     * <p><b>Post-condition:</b> {@code isRead == true}</p>
     */
    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return String.format("Notification[id=%s, type=%s, read=%s]",
                notificationId, type, isRead);
    }
}
