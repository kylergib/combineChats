package com.kgibs.combinechats.model;


import java.time.LocalDateTime;

public class ChatMessage {
    private final String platform;
    private final String username;
    private final String message;
    private final LocalDateTime timeReceived;
    private final String channelName;

    public ChatMessage(String platform, String channelName, String username, String message, LocalDateTime timeReceived) {
        this.platform = platform;
        if (platform.equalsIgnoreCase("twitch")) channelName = channelName.replace("#","");
        this.channelName = channelName;
        this.username = username;
        this.message = message;
        this.timeReceived = timeReceived;
    }
    public String getPlatform() {return platform;}

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
    public String getChannelName() {
        return channelName;
    }

    public LocalDateTime getTimeReceived() {
        return timeReceived;
    }
}
