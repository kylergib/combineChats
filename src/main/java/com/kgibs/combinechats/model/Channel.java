package com.kgibs.combinechats.model;

public class Channel {
    private String channelName;
    private String platform;
    private boolean autoConnect;
    private boolean isConnected;
    private int id;

    public Channel(int id,String channelName, String platform, boolean autoConnect) {
        this.id = id;
        this.channelName = channelName;
        this.platform = platform;
        this.autoConnect = autoConnect;
    }
    public Channel(String channelName, String platform, boolean autoConnect) {
        this.channelName = channelName;
        this.platform = platform;
        this.autoConnect = autoConnect;
    }
    public Channel() {

    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
