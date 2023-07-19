package com.kgibs.combinechats.utility;

public interface SocketConnection {
    void onSocketConnect();
    void onChatReceived(String message);
    void onChatConnected(String channelName, boolean isConnected);
}
