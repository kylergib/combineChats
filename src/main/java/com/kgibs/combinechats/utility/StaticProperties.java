package com.kgibs.combinechats.utility;

import com.kgibs.combinechats.model.Channel;
import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.service.KickClient;
import com.kgibs.combinechats.service.TwitchClient;
import com.kgibs.combinechats.service.YoutubeApi;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class StaticProperties {
    public static Settings settings;
    public static KickClient kickClient;
    public static TwitchClient twitchClient;
    public static YoutubeApi youtube;
    public static List<Channel> channelWaitingToConnect = new ArrayList<>();

    public static Map<Integer, KickChannel> kickIdToChannel;
    public static String customStyleSheet;
    public static String defaultStyleSheet;
    public static Stage mainStage;

    public static String getStyle(String color) {
        return String.format("style=\"display: inline-block; padding: 5px; color: %s\"", color);
    }
}
