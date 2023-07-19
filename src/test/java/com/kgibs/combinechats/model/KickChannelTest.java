package com.kgibs.combinechats.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KickChannelTest {

    @Test
    void testKickChannel() {
        KickChannel channel = new KickChannel(123, "TestChannel", 12345, true);
        assertEquals(123, channel.getId());
        assertEquals("TestChannel", channel.getChannelName());
        assertEquals("kick", channel.getPlatform());
        assertEquals(12345,channel.getChannelId());
        assertTrue(channel.isAutoConnect());

    }

    @Test
    void kickTest() {
        KickChannel channel = new KickChannel();
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

        channel.setChannelId(8976);
        assertEquals(8976, channel.getChannelId());
    }
}
