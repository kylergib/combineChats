package com.kgibs.combinechats.model;

public class YoutubeChannel extends Channel {
    private String videoUrl;

    public YoutubeChannel(String channelName, String videoUrl) {
        super(channelName,"youtube",false);
        this.videoUrl = videoUrl;
    }
    public YoutubeChannel(int id,String channelName, String videoUrl) {
        super(id,channelName,"youtube",false);
        this.videoUrl = videoUrl;
    }
    public YoutubeChannel() {
        super();
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
