package com.kgibs.combinechats.service;

import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.utility.MessageUtil;
import com.kgibs.combinechats.utility.SocketConnection;
import com.kgibs.combinechats.utility.StaticProperties;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.kgibs.combinechats.utility.StaticProperties.*;

public class KickClient extends WebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(KickClient.class);
    public String socketId;
    private List<String> receivedMessageIds;
    private SocketConnection kickConnection;



    public KickClient(URI serverUri, Draft_6455 draft6455, SocketConnection kickConnection) {
        super(serverUri, draft6455);
        this.kickConnection = kickConnection;
        receivedMessageIds = new ArrayList<>();
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("WebSocket connection opened");
    }

    @Override
    public void onMessage(String message) {
//        System.out.println(message);
        JSONObject jsonMessage = new JSONObject(message);
        String event = jsonMessage.getString("event");
        if (event.equals("pusher:connection_established")) {
            JSONObject data = new JSONObject(jsonMessage.getString("data"));
            socketId = data.getString("socket_id");
            kickConnection.onSocketConnect();
        } else if (event.equals("pusher_internal:subscription_succeeded"))  {
            if (jsonMessage.keySet().contains("channel")) {
                String channelString = jsonMessage.getString("channel");
                String[] splitString = channelString.split("\\.");
                List<KickChannel> findChannel = StaticProperties.settings.getKickChannels().stream().filter(channel ->
                        String.valueOf(channel.getChannelId()).equalsIgnoreCase(splitString[1])).toList();
                String channelName;
                if (findChannel.size() == 1) {
                    channelName = findChannel.get(0).getChannelName();

                } else {
                    channelName = splitString[0];
                }
                String joinString = String.format("document.body.innerHTML += " +
                                "'<div class=\"chatMessage\"><span %s >Attemepting to connect to %s. If no messages come through, check the channel ID and try again.</span></div>'",
                        getStyle(settings.getKickMessageColor()), channelName);

                kickConnection.onChatReceived(joinString);
            }
        } else if (event.equals("App\\Events\\ChatMessageEvent")) {
            String dataString = jsonMessage.getString("data");
            JSONObject data = new JSONObject(dataString);
            String messageId = data.getString("id");
            int chatId = data.getInt("chatroom_id");


            List<KickChannel> findChannel = StaticProperties.settings.getKickChannels().stream().filter(channel ->
                    chatId==channel.getChannelId()).toList();
            KickChannel channel = null;
            String channelName;
            if (findChannel.size() == 1) {
                channel = findChannel.get(0);
                channelName = channel.getChannelName();
                if (!StaticProperties.kickIdToChannel.containsKey(chatId))
                    StaticProperties.kickIdToChannel.put(chatId, findChannel.get(0));

            } else {
                channelName = String.valueOf(chatId);
            }
            String joinString = String.format("document.body.innerHTML += " +
                            "'<div class=\"chatMessage\"><span %s >Connected to %s.</span></div>'",
                    getStyle(settings.getKickMessageColor()), channelName);



            if (channel != null && !channel.isConnected()) {
                kickConnection.onChatReceived(joinString);
                kickConnection.onChatConnected(channelName, true);
                channel.setConnected(true);
            } else if (channel == null) {
                KickChannel newChannel = new KickChannel();
                newChannel.setChannelId(chatId);
                disconnectFromChat(newChannel);
                String disconnectString = String.format("document.body.innerHTML += " +
                                "'<div class=\"chatMessage\"><span %s >Disconnected from %s. Was caused by error trying to connect to chat. Retry to connect if this was unexpected.</span></div>'",
                        getStyle(settings.getKickMessageColor()), channelName);
                kickConnection.onChatReceived(disconnectString);
            }

            String messageContent = data.getString("content");


            boolean isEmote = messageContent.contains("emote");


            String messageType = data.getString("type");
            String createdAt = data.getString("created_at");
            JSONObject senderJson = data.getJSONObject("sender");
            int senderId = senderJson.getInt("id");
            String senderUserName = senderJson.getString("username");
            JSONObject identity = senderJson.getJSONObject("identity");

            String senderColor = identity.getString("color");
            if (!receivedMessageIds.contains(messageId)) {
                receivedMessageIds.add(messageId);
                String formatMessage = MessageUtil.parseMessage("Kick",
                        senderUserName,channelName,messageContent.replace("'","\\'"));
                kickConnection.onChatReceived(formatMessage);
            }

        } else if (event.equals("App\\Events\\UserBannedEvent")) {
            LOGGER.warn("User banned event");
        } else if (event.equals("App\\Events\\SubscriptionEvent")) {
            LOGGER.warn("Subscription event");
        }
        else {
            LOGGER.warn("Message not accounted for: {}",message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("Disconnected from kick");
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("Error has occurred: ", ex);
    }


    public void connectToChat(String channelName, int channelID) {
        JSONObject json = new JSONObject();
        json.put("event", "pusher:subscribe");

        JSONObject data = new JSONObject();
        data.put("auth","");
        data.put("channel",String.format("chatrooms.%d.v2", channelID));
        json.put("data",data);
        send(String.valueOf(json));
    }
    public void disconnectFromChat(KickChannel channel) {

        LOGGER.info("trying to disconnect from {} on {}",channel.getChannelName(),channel.getChannelId());

        JSONObject json = new JSONObject();
        json.put("event", "pusher:unsubscribe");

        JSONObject data = new JSONObject();
        data.put("auth","");
        data.put("channel",String.format("chatrooms.%d.v2",channel.getChannelId()));
        json.put("data",data);
        send(String.valueOf(json));
        if (channel.getChannelName() == null) return;
        String disconnectString = String.format("document.body.innerHTML += " +
                        "'<div class=\"chatMessage\"><span %s >Disconnected from %s. Messages that were already sent to the client may appear, but no new messages will be received.</span></div>'",
                getStyle(settings.getKickMessageColor()), channel.getChannelName());
        kickConnection.onChatReceived(disconnectString);
        channel.setConnected(false);

    }
}
