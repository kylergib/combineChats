package com.kgibs.combinechats.model;

import com.kgibs.combinechats.model.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChannelTest {
    @Test
    void testChannel() {
        Channel channel = new Channel();

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
    }

    @Test
    void testChannelConstructorWithParams() {
        Channel channel = new Channel(123, "TestChannel", "TestPlatform", true);

        assertEquals(123, channel.getId());
        assertEquals("TestChannel", channel.getChannelName());
        assertEquals("TestPlatform", channel.getPlatform());
        assertTrue(channel.isAutoConnect());
        assertFalse(channel.isConnected());
    }

    @Test
    void testChannelConstructorWithoutId() {
        Channel channel = new Channel("TestChannel", "TestPlatform", true);

        assertEquals(0, channel.getId()); // Default int value
        assertEquals("TestChannel", channel.getChannelName());
        assertEquals("TestPlatform", channel.getPlatform());
        assertTrue(channel.isAutoConnect());
        assertFalse(channel.isConnected());
    }
}
