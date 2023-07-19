package com.kgibs.combinechats.model;

import com.kgibs.combinechats.model.ChatMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatMessageTest {
    @Test
    void testMessage() {
        String platform = "testingplatform";
        String channelName = "TestingChannel";
        String user = "testingUser";
        String message = "this is a message test!";
        LocalDateTime localDateTime = LocalDateTime.now();
        ChatMessage chatMessage = new ChatMessage(platform,channelName,user, message, localDateTime);
        assertEquals(platform, chatMessage.getPlatform());
        assertEquals(channelName, chatMessage.getChannelName());
        assertEquals(user, chatMessage.getUsername());

        assertEquals(message, chatMessage.getMessage());

        assertEquals(localDateTime, chatMessage.getTimeReceived());
    }
}
