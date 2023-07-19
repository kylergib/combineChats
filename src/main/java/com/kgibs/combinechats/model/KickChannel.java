package com.kgibs.combinechats.model;


public class KickChannel extends Channel {
    private int channelId;

    public KickChannel(int id,String channelName,int channelId, boolean autoConnect) {
        super(id,channelName,"kick",autoConnect);
        this.channelId = channelId;
    }
    public KickChannel(String channelName,int channelId, boolean autoConnect) {
        super(channelName,"kick",autoConnect);
        this.channelId = channelId;
    }
    public KickChannel() {

    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}
