package com.kgibs.combinechats.utility;

import com.kgibs.combinechats.model.Channel;
import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.model.YoutubeChannel;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    private List<KickChannel> kickChannels;
    private List<Channel> twitchChannels;
    private YoutubeChannel youtubeChannel;
    private boolean notificationOn;
    private int notificationDuration;
    private String kickMessageColor;
    private String twitchMessageColor;
    private String youtubeMessageColor;
    private boolean darkModeOn;
    private boolean separatePlatformColors;
    private boolean showChannelNameInMessage;
    private boolean showPlatformNameInMessage;
    private boolean redirectHttpMessages;
    private SettingsChanged settingsChanged;
    private Path appFolderPath;
    public SQLiteJDBC db;



    public interface SettingsChanged {
        void onSettingReceived();
        void onAddedChannel(Channel channel);
        void onSettingsUpdate();
    }
    public Settings(SettingsChanged settingsChanged) {
        this.settingsChanged = settingsChanged;
    }
    private void getSettings() throws SQLException {

        Map<String, Object> settingsMap = db.getSettings();
        if (settingsMap == null) {
            LOGGER.info("setting defaults");
            setDefaultSettings();
        }
        else {
            LOGGER.info("retrieving settings");
            kickChannels = new ArrayList<>();
            twitchChannels = new ArrayList<>();
            notificationOn = (boolean) settingsMap.get("notificationOn");
            notificationDuration = (int) settingsMap.get("notificationDuration");
            kickMessageColor = (String) settingsMap.get("kickMessageColor");
            twitchMessageColor = (String) settingsMap.get("twitchMessageColor");
            youtubeMessageColor = (String) settingsMap.get("youtubeMessageColor");
            darkModeOn = (boolean) settingsMap.get("darkModeOn");
            showChannelNameInMessage = (boolean) settingsMap.get("showChannelNameInMessage");
            showPlatformNameInMessage = (boolean) settingsMap.get("showPlatformNameInMessage");
        }
        getChannels();
    }
    private void setDefaultSettings() {
        kickChannels = new ArrayList<>();
        twitchChannels = new ArrayList<>();
        notificationOn = false;
        notificationDuration = 0;
        kickMessageColor = "green";
        twitchMessageColor = "purple";
        youtubeMessageColor = "red";
        darkModeOn = false;
        separatePlatformColors = true;
        showChannelNameInMessage = true;
        showPlatformNameInMessage = true;
        redirectHttpMessages = false;
        try {
            db.insertSettings(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void initSettings() {
        boolean appFolderExists = Files.exists(getApplicationDataPath("combineChats"));
        if (!appFolderExists) {
            createAppDataFolder();
        }
        db = new SQLiteJDBC();
        db.initDatabase(appFolderPath.toString() + "/combineChats.db");
        try {
            getSettings();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        settingsChanged.onSettingReceived();

    }
    private void createAppDataFolder() {
        File directory = new File(appFolderPath.toUri());
        if (!directory.exists()){
            directory.mkdir();
        }
    }
    private Path getApplicationDataPath(String appName) {
        String homeDir = System.getProperty("user.home");
        String appDataDir;

        if (SystemUtils.IS_OS_WINDOWS) {
            appDataDir = System.getenv("APPDATA");
        } else if (SystemUtils.IS_OS_MAC) {
            appDataDir = Paths.get(homeDir, "Library", "Application Support").toString();
        } else {
            appDataDir = Paths.get(homeDir, ".config").toString();
        }
        appFolderPath = Paths.get(appDataDir, appName);
        return Paths.get(appDataDir, appName);
    }
    public List<KickChannel> getKickChannels() {
        return kickChannels;
    }

    public boolean addKickChannel(KickChannel kickChannel) {

        boolean addChannel = true;
        for (KickChannel channel : kickChannels) {
            if (channel.getChannelName().equals(kickChannel.getChannelName())) {
                addChannel = false;
                break;
            }
        }
        if (addChannel) {

            int id = db.insertChannel(kickChannel);
            if (id != -1) {
                kickChannel.setId(id);
                kickChannels.add(kickChannel);
                settingsChanged.onAddedChannel(kickChannel);
            }

        }
        return addChannel;
    }
//    public KickChannel findKickChannel(String channelName, int channelId) {
//        for (KickChannel channel : kickChannels) {
//            if (channel.getChannelName().equals(channelName) && channel.getChannelId() == channelId) {
//                return channel;
//            }
//        }
//        return null;
//    }

    public List<Channel> getTwitchChannels() {
        return twitchChannels;
    }

    public Channel findTwitchChannel(String channelName) {
        LOGGER.info("Trying to find: {}", channelName);
        for (Channel twitchChannel : twitchChannels) {

            if ((twitchChannel.getChannelName()).equalsIgnoreCase(channelName)) {
                return twitchChannel;
            }
        }
        return null;

    };

    public void removeChannel(Channel channel) {
        db.removeChannel(channel.getId());
        if (channel.getPlatform().equalsIgnoreCase("twitch")) {
            twitchChannels.remove(channel);
        } else if (channel.getPlatform().equalsIgnoreCase("kick")) {
            kickChannels.remove(channel);
        } else if (channel.getPlatform().equalsIgnoreCase("youtube")) {
            youtubeChannel = null;

        }

    }
    public boolean addTwitchChannel(Channel twitchChannel) {
        boolean addChannel = true;
        for (Channel channel : twitchChannels) {
            if (channel.getChannelName().equals(twitchChannel.getChannelName())) {
                addChannel = false;
                break;
            }
        }
        if (addChannel) {

            int id = db.insertChannel(twitchChannel);
            if (id != -1) {
                twitchChannel.setId(id);
                twitchChannels.add(twitchChannel);
                settingsChanged.onAddedChannel(twitchChannel);
            }
        }


        return addChannel;
    }

    public YoutubeChannel getYoutubeChannel() {
        return youtubeChannel;
    }

    public void setYoutubeChannel(YoutubeChannel youtubeChannel) {
        int id = db.insertChannel(youtubeChannel);
        if (id != -1) {
            youtubeChannel.setId(id);
            this.youtubeChannel = youtubeChannel;
            settingsChanged.onAddedChannel(youtubeChannel);
        }
    }

    public boolean isNotificationOn() {
        return notificationOn;
    }

    public void setNotificationOn(boolean notificationOn) {
        this.notificationOn = notificationOn;
    }

    public int getNotificationDuration() {
        return notificationDuration;
    }

    public void setNotificationDuration(int notificationDuration) {
        this.notificationDuration = notificationDuration;
    }

    public String getKickMessageColor() {
        return kickMessageColor;
    }

    public void setKickMessageColor(String kickMessageColor) {
        this.kickMessageColor = kickMessageColor;
    }

    public String getTwitchMessageColor() {
        return twitchMessageColor;
    }

    public void setTwitchMessageColor(String twitchMessageColor) {
        this.twitchMessageColor = twitchMessageColor;
    }

    public String getYoutubeMessageColor() {
        return youtubeMessageColor;
    }

    public void setYoutubeMessageColor(String youtubeMessageColor) {
        this.youtubeMessageColor = youtubeMessageColor;
    }

    public boolean getDarkModeOn() {
        return darkModeOn;
    }
    public void setDarkModeOn(boolean darkModeOn) {
        this.darkModeOn = darkModeOn;
    }
    public boolean isSeparatePlatformColors() {
        return separatePlatformColors;
    }

    public void setSeparatePlatformColors(boolean separatePlatformColors) {
        this.separatePlatformColors = separatePlatformColors;
    }

    public boolean isShowChannelNameInMessage() {
        return showChannelNameInMessage;
    }

    public void setShowChannelNameInMessage(boolean showChannelNameInMessage) {
        this.showChannelNameInMessage = showChannelNameInMessage;
    }

    public boolean isShowPlatformNameInMessage() {
        return showPlatformNameInMessage;
    }

    public void setShowPlatformNameInMessage(boolean showPlatformNameInMessage) {
        this.showPlatformNameInMessage = showPlatformNameInMessage;
    }

    public boolean isRedirectHttpMessages() {
        return redirectHttpMessages;
    }

    public void setRedirectHttpMessages(boolean redirectHttpMessages) {
        this.redirectHttpMessages = redirectHttpMessages;
    }
    public void updateSettings() {
        db.updateSettings(this);
        settingsChanged.onSettingsUpdate();
    }

    public void getChannels() {
        List<Channel> allChannels = db.getAllChannels();
        allChannels.forEach(channel -> {
            if (channel.getPlatform().equalsIgnoreCase("twitch") ) twitchChannels.add(channel);
            else if (channel.getPlatform().equalsIgnoreCase("kick") ) kickChannels.add((KickChannel) channel);
            else if (channel.getPlatform().equalsIgnoreCase("youtube") ) youtubeChannel = (YoutubeChannel) channel;
        });

    }

}
