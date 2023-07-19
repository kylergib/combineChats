package com.kgibs.combinechats.service;

import com.kgibs.combinechats.utility.StaticProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.kgibs.combinechats.utility.StaticProperties.getStyle;
import static com.kgibs.combinechats.utility.StaticProperties.settings;

public class YoutubeApi extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeApi.class);
    private String videoId;
    private String chatId;
    private String nextPageToken;
    private int pollingInterval;
    private boolean stopRequested;
    private YoutubeInterface youtubeInterface;
    private Map<String,String> chatIdAndName;
    private boolean restartRequested;

    public interface YoutubeInterface {
        void onYoutubeChat(String message);
        void onConnected(String channelName);
        void onStop();
        void onError(String errorString);
    }


    public YoutubeApi(YoutubeInterface youtubeInterface) {
        this.youtubeInterface = youtubeInterface;
        chatIdAndName = new HashMap<>();

    }
    public void requestStop() {
        LOGGER.info("Stop requested");
        stopRequested = true;
    }
    public void requestRestart() {
        restartRequested = true;
    }

    public void run() {
        stopRequested = false;
        restartRequested = false;

        parseVideoUrl(StaticProperties.settings.getYoutubeChannel().getVideoUrl());
        getActiveChatId(getChatId());
        if (chatId == null) {
            youtubeInterface.onError("Error, chat id is null, please check YouTube URL and try again");
            return;
        }
        try {

            getInitChatMessages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (!stopRequested) {
            if (restartRequested) {
                parseVideoUrl(StaticProperties.settings.getYoutubeChannel().getVideoUrl());
                getActiveChatId(getChatId());
                if (chatId == null) return;
                try {

                    getInitChatMessages();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                restartRequested = false;
            }
            try {
                //has to sleep for atleast the specified time that the previous request said it would poll
                Thread.sleep(pollingInterval + 200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                getNewChatMessages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        youtubeInterface.onStop();
        LOGGER.info("Youtube should of stopped");

    }
    public void getNewChatMessages() throws IOException {

        String url =
                String.format("https://yt.lemnoslife.com/noKey/liveChat/messages?" +
                                "liveChatId=%s&part=snippet,authorDetails&maxResults=2000&pageToken=%s",
                        chatId,nextPageToken);
        JSONObject jsonResponse = getJsonYoutube(url);
        parseMessages(jsonResponse);
    }
    public void getInitChatMessages() throws IOException {

        String url =
                String.format("https://yt.lemnoslife.com/noKey/liveChat/messages?" +
                                "liveChatId=%s&part=snippet,authorDetails&maxResults=2000",
                        chatId);
        JSONObject jsonResponse = getJsonYoutube(url);
        parseMessages(jsonResponse);
    }
    public void parseMessages(JSONObject json) {
        try {


            JSONArray items = json.getJSONArray("items");
            pollingInterval = json.getInt("pollingIntervalMillis");
            nextPageToken = json.getString("nextPageToken");
            if (items.length() < 1) return;
            items.forEach(item -> {
                //snippet is info about the content
                JSONObject snippet = ((JSONObject) item).getJSONObject("snippet");
                if (!snippet.getBoolean("hasDisplayContent")) return;
                String messageContent = snippet.getString("displayMessage");
                String liveChatID = snippet.getString("liveChatId");
                //authordetails gives info about use that sent message
                JSONObject authorDetails = ((JSONObject) item).getJSONObject("authorDetails");
                String displayName = authorDetails.getString("displayName");
                boolean isMod = authorDetails.getBoolean("isChatModerator");
                boolean isChatOwner = authorDetails.getBoolean("isChatOwner");
                //todo: track messages and do not send the same one twice?
                String messageId = ((JSONObject) item).getString("id");
                String sendMessage = String.format("document.body.innerHTML += " +
                        "'<div class=\"chatMessage\">" +
                        "<span %s >",getStyle(settings.getYoutubeMessageColor()));
                if (settings.isShowChannelNameInMessage() && settings.isShowPlatformNameInMessage()) {
                    sendMessage += String.format("(YouTube - %s) ",chatIdAndName.get(liveChatID).replace("'", "\\'"));
                } else if (settings.isShowChannelNameInMessage()) {
                    sendMessage += String.format("(%s) ",chatIdAndName.get(liveChatID).replace("'", "\\'"));
                } else if (settings.isShowPlatformNameInMessage()) {
                    sendMessage += "(YouTube) ";
                }
                sendMessage += String.format("%s: %s</span></span>'",
                        displayName.replace("'", "\\'"),
                        messageContent.replace("'", "\\'").replace("\n", ""));
                youtubeInterface.onYoutubeChat(sendMessage);

                LOGGER.trace("{} - {}: {}", LocalDateTime.now(),displayName.replace("'", "\\'"),
                        messageContent.replace("'", "\\'").replace("\n", ""));
                try {
                    settings.db.insertMessage("youtube",chatIdAndName.get(liveChatID).replace("'", "\\'"),
                            displayName.replace("'", "\\'"),
                            messageContent.replace("'", "\\'").replace("\n", ""),LocalDateTime.now());
                    LOGGER.debug("Added new message from: {}", displayName.replace("'", "\\'"));
                } catch (SQLException e) {
                    LOGGER.warn("Exception occurred: ",e);
                }

            });
        } catch (JSONException e) {
            LOGGER.warn("Exception has occurred: ", e);
        }
    }
    public void getActiveChatId(JSONObject json) {
        JSONArray items = json.getJSONArray("items");
        if (items.length() == 1) {
            if (!((JSONObject) items.get(0)).keySet().contains("liveStreamingDetails")) {
                youtubeInterface.onError("YouTube stream does not seem to be live. Check URL");
            }
            JSONObject liveStreamingDetails = ((JSONObject) items.get(0)).getJSONObject("liveStreamingDetails");
            JSONObject snippet = ((JSONObject) items.get(0)).getJSONObject("snippet");
            String channelTitle = snippet.getString("channelTitle");
            if (!liveStreamingDetails.keySet().contains("activeLiveChatId")) {
                String sendString = String.format("document.body.innerHTML += " +
                                "'<div class=\"chatMessage\"><span %s >Could not connect to  %s. Please check url. </span></div>'",
                        getStyle(settings.getYoutubeMessageColor()), channelTitle);
                youtubeInterface.onYoutubeChat(sendString);
                return;
            }
            chatId = liveStreamingDetails.getString("activeLiveChatId");

            String newChannelId = snippet.getString("channelId");

            chatIdAndName.put(chatId,channelTitle);
            String joinString = String.format("document.body.innerHTML += " +
                            "'<div class=\"chatMessage\"><span %s >Connected to %s</span></div>'",
                    getStyle(settings.getYoutubeMessageColor()), channelTitle);
            youtubeInterface.onYoutubeChat(joinString);
            youtubeInterface.onConnected(channelTitle);
            StaticProperties.settings.getYoutubeChannel().setChannelName(channelTitle);
        }

    }
    public void parseVideoUrl(String videoUrl) {
        videoId = videoUrl.replace("https://www.youtube.com/watch?v=","");
    }

    public JSONObject getChatId() {
        String url = String.format(
                "https://yt.lemnoslife.com/noKey/videos?part=liveStreamingDetails,snippet&id=%s",
                videoId);
        JSONObject jsonResponse = null;
        try {
            jsonResponse = getJsonYoutube(url);
        } catch (IOException e) {
            LOGGER.warn("Exception has occurred: ", e);
        }
        return jsonResponse;


    }

    public JSONObject getJsonYoutube(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code : " + conn.getResponseCode());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = reader.readLine()) != null) {
            response.append(output);
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        conn.disconnect();
        return jsonResponse;
    }


}