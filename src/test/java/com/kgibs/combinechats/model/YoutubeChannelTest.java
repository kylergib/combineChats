package com.kgibs.combinechats.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class YoutubeChannelTest {

    @Test
    void newYoutubeChannelTest() {
        YoutubeChannel channel = new YoutubeChannel(123, "TestChannel", "videoURL");
        assertEquals(123, channel.getId());
        assertEquals("TestChannel", channel.getChannelName());
        assertEquals("youtube", channel.getPlatform());
        assertEquals("videoURL",channel.getVideoUrl());
        assertFalse(channel.isAutoConnect());
    }

    @Test
    void youtubeTest() {
        YoutubeChannel channel = new YoutubeChannel();
        // Test setter and getter for id
        channel.setId(123);
        assertEquals(123, channel.getId());

        // Test setter and getter for channelName
        channel.setChannelName("TestChannel");
        assertEquals("TestChannel", channel.getChannelName());

        // Test setter and getter for platform
        channel.setPlatform("TestPlatform");
        assertEquals("TestPlatform", channel.getPlatform());

        // Test setter and getter for autoConnect
        channel.setAutoConnect(true);
        assertTrue(channel.isAutoConnect());

        // Test setter and getter for isConnected
        channel.setConnected(true);
        assertTrue(channel.isConnected());

        channel.setVideoUrl("testurl");
        assertEquals("testurl", channel.getVideoUrl());
    }

}
