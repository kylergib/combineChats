package com.kgibs.combinechats.service;

import com.kgibs.combinechats.utility.MessageUtil;
import com.kgibs.combinechats.utility.SocketConnection;
import com.kgibs.combinechats.utility.TwitchParse;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.kgibs.combinechats.utility.StaticProperties.*;

public class TwitchClient extends WebSocketClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchClient.class);
    private final SocketConnection twitchConnection;


    public TwitchClient(URI serverUri, SocketConnection twitchConnection) {
        super(serverUri);
        this.twitchConnection = twitchConnection;
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

        LOGGER.info("Connected to twitch");
        send("PASS SCHMOOPIIE");
        send("NICK justinfan32580");
    }

    @Override
    public void onMessage(String message) {
        LOGGER.trace(message);
        JSONObject parsedMessage = TwitchParse.parseMessage(message);

        String channelName;
        if (parsedMessage.keySet().contains("command")) {
            String command = parsedMessage.getString("command");
            switch (command) {
                case "001" -> twitchConnection.onSocketConnect();
                case "PART" -> {
                    channelName = parsedMessage.getString("channel");
                    partCommand(channelName);
                }
                case "JOIN" -> {
                    channelName = parsedMessage.getString("channel");
                    joinCommand(channelName);
                }
                case "PING" -> {
                    String content = parsedMessage.getString("content");
                    send("PONG " + content);
                }
                case "PRIVMSG" -> {
                    privMesgCommand(parsedMessage);
                }
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("Disconnected from twitch");
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("Error has occurred: ", ex);
    }

    private void partCommand(String channelName) {
        String[] splitChannel = channelName.split("\\r\\n");
        String disconnectString = String.format("document.body.innerHTML += " +
                        "'<div class=\"chatMessage\"><span %s >Disconnected from %s</span></div>'",
                getStyle(settings.getTwitchMessageColor()), splitChannel[0]);
        twitchConnection.onChatReceived(disconnectString);
        twitchConnection.onChatConnected(splitChannel[0], false);
    }
    private void joinCommand(String channelName) {
        String[] splitChannel = channelName.split("\\r\\n");
        String joinString = String.format("document.body.innerHTML += " +
                        "'<div class=\"chatMessage\"><span %s >Connected to %s</span></div>'",
                getStyle(settings.getTwitchMessageColor()), splitChannel[0]);
        twitchConnection.onChatReceived(joinString);
        twitchConnection.onChatConnected(splitChannel[0], true);
    }
    private void privMesgCommand(JSONObject parsedMessage) {
        String content = parsedMessage.getString("content").replace("'", "\\'");
        if (content.startsWith("ACTION")) content = content.replace("ACTION ", "");
        String[] contentList = content.split(" ");
        String userName = parsedMessage.getString("nick");
        String channelName = parsedMessage.getString("channel");

        JSONObject tags = parsedMessage.getJSONObject("tags");
        Object emotes = tags.get("emotes");
//        String newContent = " " + content + " ";
        if (emotes.getClass().equals(JSONObject.class)) {
            List<String> keys = new ArrayList<String>(((JSONObject) emotes).keySet());

            for (int z = 0; z < ((JSONObject) emotes).length(); z++) {
                String id = keys.get(z);
                JSONArray emote = ((JSONObject) emotes).getJSONArray(id);

                for (int i = 0; i < emote.length(); i++) {
                    JSONObject pos = emote.getJSONObject(i);
                    int startPosition = Integer.parseInt(((JSONObject) pos).getString("startPosition")) ;
                    int endPosition = Integer.parseInt((((JSONObject) pos).getString("endPosition")));
                    if (startPosition != 0) startPosition -= 1;
                    String emoteString = content.substring(startPosition, endPosition + 1).trim();
//                    String emoteStringWithSpace = " " + emoteString + " ";
                    for (int index = 0; index < contentList.length; index++) {
                        if (contentList[index].equals(emoteString)) contentList[index] = String.format("[emote:%s]", id);
                    }
                }
            }
        }
        String testMessage = "";
        for (int index = 0; index < contentList.length; index++) {
            if (testMessage.equals("")) testMessage += contentList[index];
            else testMessage += " " + contentList[index];
        }
        String formatMessage = MessageUtil.parseMessage("Twitch",userName,
                channelName,testMessage);
        LOGGER.debug(formatMessage);
        if (formatMessage != null)
            twitchConnection.onChatReceived(formatMessage);
    }
}

