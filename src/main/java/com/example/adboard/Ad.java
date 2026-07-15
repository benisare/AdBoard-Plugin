package com.example.adboard;

import java.util.UUID;

/**
 * A single service ad posted by a player.
 */
public class Ad {

    private final UUID owner;
    private final String ownerName;
    private final String message;
    private final long timestamp;

    public Ad(UUID owner, String ownerName, String message, long timestamp) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
