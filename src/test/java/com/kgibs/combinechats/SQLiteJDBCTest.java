package com.kgibs.combinechats;

import com.kgibs.combinechats.model.Channel;
import com.kgibs.combinechats.model.ChatMessage;
import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.model.YoutubeChannel;
import com.kgibs.combinechats.utility.SQLiteJDBC;
import com.kgibs.combinechats.utility.Settings;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteJDBCTest {
    private SQLiteJDBC db;

    @BeforeEach
    void setUp() {
        db = new SQLiteJDBC();
        db.initDatabase("unitTest.db");
    }
    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Path.of("unitTest.db"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void initDatabaseTest() {

        List<String> tableNames = db.listTables();
        assertTrue(tableNames.contains("settings"), "Settings Table does not exist");
        assertTrue(tableNames.contains("messages"), "Messages Table does not exist");
        assertTrue(tableNames.contains("channels"), "Channels Table does not exist");

    }
    @Test
    void messagesCRUDTest() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String platform = "testplatform";
        String user = "testUser";
        String message = "This is a test message";
        String channelName = "TestChannel";
        int newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        boolean insertSuccessful = newMessageId != -1;
        assertTrue(insertSuccessful, "Insert message was not successful");
        ChatMessage chatMessage = db.getMessage(newMessageId);
        assertEquals(chatMessage.getPlatform(),platform, "Channel name on messages do not match");
        assertEquals(chatMessage.getChannelName(),channelName, "Channel name on messages do not match");
        assertEquals(chatMessage.getUsername(),user, "User name on messages do not match");
        assertEquals(chatMessage.getMessage(),message, "Chat messages do not match");
        assertEquals(chatMessage.getTimeReceived(),localDateTime, "TimeReceived on messages do not match");
    }
    @Test
    void messagesValidationTest() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String platform = null;
        String user = "testUser";
        String message = "This is a test message";
        String channelName = "TestChannel";
        int newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        boolean insertSuccessful = newMessageId != -1;
        assertFalse(insertSuccessful, "Insert message was successful");

        platform = "testPlatform";
        user = null;
        newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        insertSuccessful = newMessageId != -1;
        assertFalse(insertSuccessful, "Insert message was successful");

        user = "testUser";
        message = null;
        newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        insertSuccessful = newMessageId != -1;
        assertFalse(insertSuccessful, "Insert message was successful");

        message = "test message";
        channelName = null;
        newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        insertSuccessful = newMessageId != -1;
        assertFalse(insertSuccessful, "Insert message was successful");

        channelName = "test channel";
        localDateTime = null;
        newMessageId = db.insertMessage(platform,channelName,user,message, localDateTime);
        insertSuccessful = newMessageId != -1;
        assertFalse(insertSuccessful, "Insert message was successful");


    }
    @Test
    void settingsCRUDTest() throws SQLException {
        Settings testSettings = new Settings(new Settings.SettingsChanged() {
            @Override
            public void onSettingReceived() {

            }

            @Override
            public void onAddedChannel(Channel channel) {

            }

            @Override
            public void onSettingsUpdate() {

            }
        });
        boolean notificationOn = false;
        int notificationDuration = 0;
        String kickMessageColor = "greenTs";
        String twitchMessageColor = "purpletest";
        String youtubeMessageColor = "redtest";
        boolean darkModeOn = true;
        boolean separatePlatformColors = false;
        boolean showChannelNameInMessage = true;
        boolean showPlatformNameInMessage = false;
        boolean redirectHttpMessages = false;
        testSettings.setNotificationOn(notificationOn);
        testSettings.setNotificationDuration(notificationDuration);
        testSettings.setKickMessageColor(kickMessageColor);
        testSettings.setTwitchMessageColor(twitchMessageColor);
        testSettings.setYoutubeMessageColor(youtubeMessageColor);
        testSettings.setDarkModeOn(darkModeOn);
        testSettings.setShowChannelNameInMessage(showChannelNameInMessage);
        testSettings.setShowPlatformNameInMessage(showPlatformNameInMessage);
        db.insertSettings(testSettings);
        Map<String, Object> getSettings = db.getSettings();
        assertEquals(getSettings.get("notificationOn"),notificationOn,"NotifcationOn does not match");
        assertEquals(getSettings.get("notificationDuration"),notificationDuration,"notificationDuration does not match");
        assertEquals(getSettings.get("kickMessageColor"),kickMessageColor,"kickMessageColor does not match");
        assertEquals(getSettings.get("twitchMessageColor"),twitchMessageColor,"twitchMessageColor does not match");
        assertEquals(getSettings.get("youtubeMessageColor"),youtubeMessageColor,"youtubeMessageColor does not match");
        assertEquals(getSettings.get("darkModeOn"),darkModeOn,"darkModeOn does not match");
        assertEquals(getSettings.get("showChannelNameInMessage"),showChannelNameInMessage,"showChannelNameInMessage does not match");
        assertEquals(getSettings.get("showPlatformNameInMessage"),showPlatformNameInMessage,"showPlatformNameInMessage does not match");
        String newColor = "NEWCOLOR";
        testSettings.setTwitchMessageColor(newColor);
        int id = db.updateSettings(testSettings);
        assertTrue(id != -1, "Update settings was unsuccessful");
        Map<String, Object> getNewSettings = db.getSettings();
        assertEquals(getNewSettings.get("notificationOn"),notificationOn,"NotifcationOn does not match");
        assertEquals(getNewSettings.get("notificationDuration"),notificationDuration,"notificationDuration does not match");
        assertEquals(getNewSettings.get("kickMessageColor"),kickMessageColor,"kickMessageColor does not match");
        assertEquals(getNewSettings.get("twitchMessageColor"),newColor,"twitchMessageColor does not match");
        assertEquals(getNewSettings.get("youtubeMessageColor"),youtubeMessageColor,"youtubeMessageColor does not match");
        assertEquals(getNewSettings.get("darkModeOn"),darkModeOn,"darkModeOn does not match");
        assertEquals(getNewSettings.get("showChannelNameInMessage"),showChannelNameInMessage,"showChannelNameInMessage does not match");
        assertEquals(getNewSettings.get("showPlatformNameInMessage"),showPlatformNameInMessage,"showPlatformNameInMessage does not match");




    }
    @Test
    void channelsCRUDTest() {
        String twitchName = "testTwitchName";
        Channel twitchChannel = new Channel(twitchName,"twitch",true);
        int twitchId = db.insertChannel(twitchChannel);
        twitchChannel.setId(twitchId);
        assertTrue(twitchId != -1, "Insert twitch channel was unsuccessful");
        Channel getTwitchChannel = db.getChannel(twitchId);
        assertEquals(getTwitchChannel.getChannelName(),twitchName,"Twitch channel names don't match");
        assertEquals(getTwitchChannel.getId(),twitchId,"Twitch ids don't match");
        assertEquals(getTwitchChannel.getPlatform(),"twitch", "Twitch platforms don't match");
        assertEquals(getTwitchChannel.isAutoConnect(),true, "Twitch autoconnect don't match");



        String kickName = "testKickName";
        KickChannel kickChannel = new KickChannel(kickName,12345,false);
        int kickId = db.insertChannel(kickChannel);
        kickChannel.setId(kickId);
        assertTrue(kickId != -1, "Insert kick channel was unsuccessful");
        Channel getKickChannel = db.getChannel(kickId);
        assertEquals(getKickChannel.getChannelName(),kickName,"Kick channel names don't match");
        assertEquals(getKickChannel.getId(),kickId,"Kick ids don't match");
        assertEquals(getKickChannel.getPlatform(),"kick", "Kick platforms don't match");
        assertEquals(getKickChannel.isAutoConnect(),false, "Kick autoconnect don't match");
        assertEquals(((KickChannel) getKickChannel).getChannelId(), 12345, "Kick chat id's don't match");



        String youtubeName = "testYoutubeName";
        String tempURL = "tempURL";
        YoutubeChannel youtubeChannel = new YoutubeChannel(youtubeName,tempURL);
        int youtubeId = db.insertChannel(youtubeChannel);
        youtubeChannel.setId(youtubeId);
        assertTrue(youtubeId != -1, "Insert kick channel was unsuccessful");
        Channel getYoutubeChannel = db.getChannel(youtubeId);
        assertEquals(getYoutubeChannel.getChannelName(),youtubeName,"Youtube channel names don't match");
        assertEquals(getYoutubeChannel.getId(),youtubeId,"Youtube ids don't match");
        assertEquals(getYoutubeChannel.getPlatform(),"youtube", "Youtube platforms don't match");
        assertEquals(getYoutubeChannel.isAutoConnect(),false, "Youtube autoconnect don't match");
        assertEquals(((YoutubeChannel) getYoutubeChannel).getVideoUrl(), tempURL, "Youtube urls don't match");

        db.removeChannel(youtubeId);
        Channel getNewYoutubeChannel = db.getChannel(youtubeId);
        assertTrue(getNewYoutubeChannel == null, "Channel was not deleted successfully");


    }





}
