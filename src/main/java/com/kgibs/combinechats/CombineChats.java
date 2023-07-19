package com.kgibs.combinechats;

import com.kgibs.combinechats.utility.StaticProperties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kgibs.combinechats.utility.StaticProperties.*;

public class CombineChats extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(CombineChats.class);
    public static Scene mainScene;
    @Override
    public void start(Stage stage) throws IOException {

        LOGGER.info("Start");
        FXMLLoader fxmlLoader = new FXMLLoader(CombineChats.class.getResource("home.fxml"));
        mainScene = new Scene(fxmlLoader.load(), 400, 1000);
        stage.setTitle("Combine Chats");
        stage.setScene(mainScene);
        defaultStyleSheet = getClass().getResource("modena.css").toExternalForm();
        customStyleSheet = getClass().getResource("combineChats.css").toExternalForm();

        LOGGER.info("Dark mode is on: {}", settings.getDarkModeOn());
        if (settings.getDarkModeOn()) {
            mainScene.getStylesheets().addAll(defaultStyleSheet,customStyleSheet);

        }


        stage.show();
        mainStage = stage;
    }
    @Override
    public void stop() throws Exception {
        LOGGER.info("trying to close app");
        if (StaticProperties.kickClient != null && StaticProperties.kickClient.isOpen()) StaticProperties.kickClient.close();
        if (StaticProperties.twitchClient != null && StaticProperties.twitchClient.isOpen()) StaticProperties.twitchClient.close();
        if (StaticProperties.youtube != null && StaticProperties.youtube.isAlive()) StaticProperties.youtube.requestStop();
        LOGGER.info("closed all clients");
        Platform.exit();
    }

    public static void main(String[] args) {
        launch();
    }
}