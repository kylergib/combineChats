package com.kgibs.combinechats.controller;

import com.kgibs.combinechats.CombineChats;
import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.utility.Settings;
import com.kgibs.combinechats.model.Channel;
import com.kgibs.combinechats.service.*;
import com.kgibs.combinechats.utility.SocketConnection;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.java_websocket.drafts.Draft_6455;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.kgibs.combinechats.utility.StaticProperties.*;

public class HomeController implements Initializable,
        Settings.SettingsChanged
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PopupController.class);
    public WebView chatWebView;
    public WebEngine webEngine;
    private static SocketConnection twitchConnection;
    private static SocketConnection kickConnection;
    private static YoutubeApi.YoutubeInterface youtubeInterface;
    public MenuItem addTwitchMenuItem;
    public MenuItem addKickMenuItem;
    public MenuItem addYoutubeMenuItem;
    public Menu twitchMenu;
    public Menu kickMenu;
    public Menu youtubeMenu;
    public VBox mainVBox;
    public Label statusLabel;
    public CheckBox scrollCheckbox;
    private double oldScrollValue;
    private double maxScrollValue;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initializing");
        connectToKick();
        connectToTwitch();
        kickIdToChannel = new HashMap<>();
        settings = new Settings(this);
        settings.initSettings();
        webEngine = chatWebView.getEngine();
        String backgroundColor = "white";
        if (settings.getDarkModeOn()) backgroundColor = "#292929";
        String content = String.format("<html><body style=\"overflow-x:hidden!important;background-color: %s;border-color: red; border-width: 10px;border-radius: 10px;\">" +
                "</body></html>", backgroundColor);
        webEngine.loadContent(content);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
            LOGGER.trace("Old value: {}", oldValue);
            LOGGER.trace("New value: {}", newValue);
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaFXApp", this);
                webEngine.executeScript(
                        "window.onscroll = function() {"
                                + "    javaFXApp.onScroll(window.pageYOffset,document.body.scrollHeight);"
                                + "};"
                );
            }
        });




    }
    public void onScroll(double scrollY,double testingMax) {
        LOGGER.trace("Testing max: {}", testingMax);
        LOGGER.trace("scrollY: {} - maxScroll: {}", scrollY,maxScrollValue);
        if (scrollY < oldScrollValue) {
            LOGGER.trace("Scroll is: {} and is lower than old value of: {}", scrollY,oldScrollValue);
            LOGGER.trace("Setting scroll checkbox to false because scroll");
            scrollCheckbox.setSelected(false);
        }
        oldScrollValue = scrollY;
//        if (scrollY == maxScrollValue) {
//            LOGGER.trace("Scroll is: {} and is equal to max scroll value of: {}", scrollY,maxScrollValue);
//            LOGGER.trace("Setting scroll checkbox to true because scroll");
//            scrollCheckbox.setSelected(true);
//        }
//        if (scrollY > maxScrollValue) {
//            maxScrollValue = scrollY;
//        }
    }

    public void connectToYouTube() {
        if (settings.getYoutubeChannel() == null) return;
        LOGGER.info("Trying to connect to youtube");
        youtubeInterface = new YoutubeApi.YoutubeInterface() {
            @Override
            public void onYoutubeChat(String message) {
                Platform.runLater(() -> {
                    addMessageToChat(message);
                });
            }

            @Override
            public void onConnected(String channelName) {
                Platform.runLater(() -> {
                    Menu menu = (Menu) youtubeMenu.getItems().get(0);
                    menu.setText(channelName);
                    MenuItem menuItem = menu.getItems().get(0);
                    menuItem.setText("Disconnect");
                    menuItem.setDisable(false);
                });
            }

            @Override
            public void onStop() {
                Platform.runLater(() -> {
                    if (settings.getYoutubeChannel() != null) {
                        Menu menu = (Menu) youtubeMenu.getItems().get(0);
                        MenuItem menuItem = menu.getItems().get(0);
                        menuItem.setText("Connect");
                    }

                    String joinString = String.format("document.body.innerHTML += " +
                                    "'<div class=\"chatMessage\"><span %s >Disconnected from YouTube</span></div>'",
                            getStyle(settings.getYoutubeMessageColor()));
                    addMessageToChat(joinString);

                });
            }

            @Override
            public void onError(String errorString) {
                String sendMessage = String.format("document.body.innerHTML += " +
                                "'<div class=\"chatMessage\">" +
                                "<span %s >%s</span></span>'",
                        getStyle(settings.getYoutubeMessageColor()),errorString);
                addMessageToChat(sendMessage);

            }
        };
        if (youtube != null && youtube.isAlive()) {
            return;
        }
        youtube = new YoutubeApi(youtubeInterface);
        youtube.start();

    }
    public void connectToKick() {
        LOGGER.info("starting connectToKick");
        kickConnection = new SocketConnection() {
            @Override
            public void onSocketConnect() {
                List<String> connectedChannels = new ArrayList<>();
                for (KickChannel channel: settings.getKickChannels()) {
                    if (channel.isAutoConnect() && !connectedChannels.contains(channel.getChannelName())) {
                        connectToChat(channel);
                        connectedChannels.add(channel.getChannelName());
                    }
                }
                for (int i = channelWaitingToConnect.size() - 1; i >= 0; i--) {
                    Channel channel = channelWaitingToConnect.get(i);
                    if (channel.getPlatform().equalsIgnoreCase("kick")  && !connectedChannels.contains(channel.getChannelName())) {
                        channelWaitingToConnect.remove(channel);
                        connectToChat(channel);

                        connectedChannels.add(channel.getChannelName());
                    }
                }
            }

            @Override
            public void onChatReceived(String message) {
                Platform.runLater(() -> {
                    addMessageToChat(message);
                    if (scrollCheckbox.isSelected())
                        webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);");


                });
            }

            @Override
            public void onChatConnected(String channelName, boolean isConnected) {
                Platform.runLater(() -> {
                    for (Object menu : kickMenu.getItems()) {
                        if (menu.getClass() == Menu.class) {
                            if ((((Menu) menu).getText()).equalsIgnoreCase(channelName)) {

                                String setText;
                                if (isConnected) setText = "Disconnect";
                                else setText = "Connect";
                                ((Menu) menu).getItems().get(0).setText(setText);
                                ((Menu) menu).getItems().get(0).setDisable(false);

                            }
                        }
                    }
                });
            }
        };
        String url = "wss://ws-us2.pusher.com/app/eb1d5f283081a78b932c?protocol=7&client=js&version=7.6.0&flash=false";
        try {
            kickClient = new KickClient(new URI(url), new Draft_6455(), kickConnection);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToTwitch() {
        String twitchUrlSSL = "wss://irc-ws.chat.twitch.tv:443";
        if (twitchClient != null && twitchClient.isOpen()) twitchClient.close();
        twitchConnection = new SocketConnection() {
            @Override
            public void onSocketConnect() {
                LOGGER.info("onSocket");
                twitchClient.send("CAP REQ :twitch.tv/commands twitch.tv/tags");
                for (Channel channel: settings.getTwitchChannels()) {
                    if (channel.isAutoConnect()) connectToChat(channel);
                }
                for (int i = channelWaitingToConnect.size() - 1; i >= 0; i--) {
                    Channel channel = channelWaitingToConnect.get(i);
                    if (channel.getPlatform().equalsIgnoreCase("twitch")) {
                        connectToChat(channel);
                        channelWaitingToConnect.remove(channel);
                    }
                }


            }

            @Override
            public void onChatConnected(String channelName, boolean isConnected) {
                Platform.runLater(() -> {
                    LOGGER.debug("Connected to chat: {}",channelName);
                    for (Object menu : twitchMenu.getItems()) {
                        if (menu.getClass() == Menu.class) {
                            if (("#" + ((Menu) menu).getText()).equalsIgnoreCase(channelName)) {

                                String setText;
                                if (isConnected) setText = "Disconnect";
                                else setText = "Connect";
                                ((Menu) menu).getItems().get(0).setText(setText);
                                ((Menu) menu).getItems().get(0).setDisable(false);
                                Channel channel = settings.findTwitchChannel(((Menu) menu).getText());
                                if (channel != null) channel.setConnected(isConnected);

                            }
                        }
                    }
                });
            }

            @Override
            public void onChatReceived(String message) {
                Platform.runLater(() -> {
                    addMessageToChat(message);
                    if (scrollCheckbox.isSelected())
                        webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                });
            }
        };
        try {
            twitchClient = new TwitchClient(new URI(twitchUrlSSL), twitchConnection);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }
    public void addMessageToChat(String message) {
        Platform.runLater(() -> {

            try {
                webEngine.executeScript(message);
            } catch (JSException ex) {
                LOGGER.warn(message);
                LOGGER.error("An error occurred",ex);
            }
            String script =
                    "var divElements = document.getElementsByClassName('chatMessage');\n" +
                            "if(divElements.length > 1000) { divElements[0].parentNode.removeChild(divElements[0]);}";
            try {
                if (scrollCheckbox.isSelected()) {
                    webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    webEngine.executeScript(script);
                }
            } catch (JSException ex) {
                LOGGER.warn(script);
                LOGGER.error("An error occurred",ex);
            }

        });
    }

    public void settingMenuItemClicked() {
        try {
            Stage childStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(CombineChats.class.getResource("settings.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 425, 200);

            double mainStageX = mainStage.getX();
            double mainStageY = mainStage.getY();
            childStage.setX(mainStageX);
            childStage.setY(mainStageY+200);


            LOGGER.info("Dark mode is on: {}", settings.getDarkModeOn());
            if (settings.getDarkModeOn()) {
                scene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

            }
            childStage.setTitle("Combine Chats Settings");
            childStage.setScene(scene);
            childStage.setAlwaysOnTop(true);
            childStage.setOnCloseRequest(event -> {
                mainVBox.setDisable(false);
            });
            mainVBox.setDisable(true);
            childStage.setAlwaysOnTop(true);

            childStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeMenuItemClicked() {
        System.exit(0);
    }

    public void addTwitchMenuItemClicked() {
        addChannelPopup("Twitch");
    }
    public void addChannelPopup(String platform) {
        try {
            Stage childStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(CombineChats.class.getResource("addChannel.fxml"));
            double mainStageX = mainStage.getX();
            double mainStageY = mainStage.getY();
            childStage.setX(mainStageX);
            childStage.setY(mainStageY+200);

            Scene childScene = new Scene(fxmlLoader.load(), 350, 215);
            LOGGER.info("Dark mode is on: {}", settings.getDarkModeOn());
            if (settings.getDarkModeOn()) {
                childScene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

            }
            PopupController popupController = fxmlLoader.getController();
            popupController.setPlatform(platform);
            childStage.setTitle("Add " + platform + " Channel");
            childStage.setScene(childScene);
            childStage.setOnCloseRequest(event -> {
                mainVBox.setDisable(false);
            });
            mainVBox.setDisable(true);
            childStage.setAlwaysOnTop(true);
            childStage.show();
            LOGGER.info("Starting popup window");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void addKickMenuItemCicked() {
        addChannelPopup("Kick");
    }

    public void addYoutubeMenuItemClicked() {
        addChannelPopup("YouTube");
    }

    public void connectToChat(Channel channel) {
        LOGGER.info("Trying to connect to: {} on {}",channel.getChannelName(),channel.getPlatform());
        boolean platformIsTwitch = channel.getPlatform().equalsIgnoreCase("twitch");
        boolean platformIsKick = channel.getPlatform().equalsIgnoreCase("kick");
        boolean platformIsYoutube = channel.getPlatform().equalsIgnoreCase("youtube");
        if (platformIsTwitch) {
            twitchClient.send("JOIN #" + channel.getChannelName());
            if (twitchMenu.getItems().get(0) instanceof Menu) {
                twitchMenu.getItems().forEach(menu -> {
                    if (menu instanceof Menu && menu.getText().equalsIgnoreCase(channel.getChannelName())) {
                        ((Menu) menu).getItems().get(0).setDisable(true);
                        LOGGER.debug("Disabled {} channel connect menu.", menu.getText());
                    }
                });
            }
        } else if (platformIsKick) {

            kickClient.connectToChat(channel.getChannelName(),((KickChannel) channel).getChannelId());
            ((Menu) kickMenu.getItems().stream().filter(menu ->
                    menu.getText().equals(channel.getChannelName())).findFirst().
                    get()).getItems().get(0).setText("Disconnect");
        } else if (platformIsYoutube) {
            connectToYouTube();
            ((Menu) youtubeMenu.getItems().get(0)).getItems().get(0).setDisable(true);

        }
    }
    public void disconnectFromChat(Channel channel) {
        LOGGER.info("Trying to disconnect to: {} on {}",channel.getChannelName(),channel.getPlatform());
        boolean platformIsTwitch = channel.getPlatform().equalsIgnoreCase("twitch");
        boolean platformIsKick = channel.getPlatform().equalsIgnoreCase("kick");
        boolean platformIsYoutube = channel.getPlatform().equalsIgnoreCase("youtube");
        if (platformIsTwitch && twitchClient != null) twitchClient.send("PART #" + channel.getChannelName());
        if (platformIsKick && kickClient != null) kickClient.disconnectFromChat((KickChannel) channel);
        if (platformIsYoutube && youtube != null) youtube.requestStop();
    }
    @Override
    public void onSettingReceived() {
        LOGGER.info("Received settings");
        for (Channel channel: settings.getTwitchChannels()) {
            onAddedChannel(channel);
        }
        for (Channel channel: settings.getKickChannels()) {
            onAddedChannel(channel);
        }
        if (settings.getYoutubeChannel() != null) {
            onAddedChannel(settings.getYoutubeChannel());
        }
    }

    @Override
    public void onAddedChannel(Channel channel) {
        boolean isTwitchChannel = channel.getPlatform().equalsIgnoreCase("twitch");
        boolean isKickChannel = channel.getPlatform().equalsIgnoreCase("kick");
        boolean isYouTubeChannel = channel.getPlatform().equalsIgnoreCase("youtube");
        Platform.runLater(() -> {
            Menu menu;
            if (isTwitchChannel) {
                menu = twitchMenu;
            } else if (isKickChannel) {
                menu = kickMenu;
            } else if (isYouTubeChannel) {
                menu = youtubeMenu;
                if (menu.getItems().size() > 1) {
                    menu.getItems().remove(0);
                }
                menu.getItems().get(0).setText("Update URL");
            }
            else {
                return;
            }

            Menu newChannelMenu = new Menu(channel.getChannelName());
            MenuItem connectMenu = new MenuItem("Connect");
            connectMenu.setOnAction(e -> {
                if (connectMenu.getText().equalsIgnoreCase("connect")) {
                    if (isTwitchChannel && twitchClient == null) {
                        connectToTwitch();
                        channelWaitingToConnect.add(channel);
                    } else if (isKickChannel && kickClient == null) {
                        connectToKick();
                        channelWaitingToConnect.add(channel);
                    } else if (isYouTubeChannel && youtube == null) {
                        connectToYouTube();
                    } else {
                        connectToChat(channel);
                    }


                } else if (connectMenu.getText().equalsIgnoreCase("disconnect")) {
                    if (isTwitchChannel && twitchClient == null) {
                        connectMenu.setText("Connect");
                    } else if (isKickChannel && kickClient == null) {
                        connectMenu.setText("Connect");
                    } else if (isYouTubeChannel && youtube == null) {
                        connectMenu.setText("Connect");
                    } else {
                        disconnectFromChat(channel);
                        if (!isYouTubeChannel) connectMenu.setText("Connect");
                    }
                }
            });
            MenuItem deleteMenu = new MenuItem("delete");
            deleteMenu.setOnAction(e -> {
                settings.removeChannel(channel);
                if (channel.isConnected())
                    disconnectFromChat(channel);
                menu.getItems().remove(newChannelMenu);
                if (isYouTubeChannel) {
                    menu.getItems().get(0).setText("Add");
                }
            });
            if (!channel.getPlatform().equalsIgnoreCase("youtube")) {
                MenuItem autoConnectMenu = new MenuItem("Auto Connect: " + channel.isAutoConnect());
                autoConnectMenu.setOnAction(e -> {

                    channel.setAutoConnect(!channel.isAutoConnect());
                    autoConnectMenu.setText("Auto Connect: " + channel.isAutoConnect());
                    //TODO: possibly need to update
                    settings.db.updateChannel(channel);


                });
                newChannelMenu.getItems().addAll(connectMenu,autoConnectMenu,deleteMenu);

            } else {
                newChannelMenu.getItems().addAll(connectMenu,deleteMenu);
            }
            menu.getItems().add(menu.getItems().size()-1,newChannelMenu);
            if (channel.getPlatform().equalsIgnoreCase("kick")) {
                MenuItem channelSettings = new MenuItem("Channel Settings");
                channelSettings.setOnAction(e -> {
                    try {
                        Stage childStage = new Stage();
                        FXMLLoader fxmlLoader = new FXMLLoader(CombineChats.class.getResource("addChannel.fxml"));
                        double mainStageX = mainStage.getX();
                        double mainStageY = mainStage.getY();
                        childStage.setX(mainStageX);
                        childStage.setY(mainStageY+200);

                        Scene childScene = new Scene(fxmlLoader.load(), 350, 215);
                        LOGGER.info("Dark mode is on: {}", settings.getDarkModeOn());
                        if (settings.getDarkModeOn()) {
                            childScene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

                        }
                        PopupController popupController = fxmlLoader.getController();
                        popupController.setPlatform("kick");
                        popupController.channelNameInput.setText(channel.getChannelName());
                        popupController.channelIdInput.setText(String.valueOf(((KickChannel) channel).getChannelId()));
                        popupController.channelNameInputTextChanged();
                        popupController.addButton.setText("Update");
                        popupController.currentKickChannel = (KickChannel) channel;
                        childStage.setTitle("Add " + "kick" + " Channel");
                        childStage.setScene(childScene);
                        childStage.setOnCloseRequest(event -> {
                            mainVBox.setDisable(false);
                        });
                        mainVBox.setDisable(true);
                        childStage.setAlwaysOnTop(true);
                        childStage.show();
                    } catch (IOException z) {
                        throw new RuntimeException(z);
                    }
                });
                newChannelMenu.getItems().add(newChannelMenu.getItems().size()-1,channelSettings);
            }

        });
        if (channel.isAutoConnect() && isKickChannel) {
            if (kickClient == null) {
                channelWaitingToConnect.add(channel);
                connectToKick();
            } else if (kickClient.isOpen()) {
                connectToChat(channel);
            }

        } else if (channel.isAutoConnect() && isTwitchChannel) {
            if (twitchClient == null) {
                channelWaitingToConnect.add(channel);
                connectToTwitch();
            } else if (twitchClient.isOpen()) {
                connectToChat(channel);
            }
        }


    }

    @Override
    public void onSettingsUpdate() {
        Scene scene = chatWebView.getScene();
        if (scene != null && settings.getDarkModeOn()) {

            boolean hasDefaultStyleSheet = scene.getStylesheets().contains(defaultStyleSheet);
            boolean hasCustomStyleSheet = scene.getStylesheets().contains(customStyleSheet);
            webEngine.executeScript("document.body.style.backgroundColor = \"#292929\";");
            if (!hasDefaultStyleSheet && !hasCustomStyleSheet)
                scene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

        } else if (scene != null) {
            scene.getStylesheets().removeAll(defaultStyleSheet,customStyleSheet);
            webEngine.executeScript("document.body.style.backgroundColor = \"white\";");
        }

    }

    public void reportMenuClicked() throws IOException {

        Stage childStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(CombineChats.class.getResource("reports.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        double mainStageX = mainStage.getX();
        double mainStageY = mainStage.getY();
        childStage.setX(mainStageX);
        childStage.setY(mainStageY+200);


        LOGGER.info("Dark mode is on: {}", settings.getDarkModeOn());
        if (settings.getDarkModeOn()) {
            scene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

        }
        childStage.setTitle("Combine Chats Reports");
        childStage.setScene(scene);
        childStage.setOnCloseRequest(event -> {
            mainVBox.setDisable(false);
        });
        mainVBox.setDisable(true);
        childStage.show();
    }
}