package com.kgibs.combinechats.controller;

import com.kgibs.combinechats.model.Channel;
import com.kgibs.combinechats.model.KickChannel;
import com.kgibs.combinechats.model.YoutubeChannel;
import com.kgibs.combinechats.utility.LabelHandler;
import com.kgibs.combinechats.utility.StaticProperties;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

//TODO: set labels for channel settings
public class PopupController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopupController.class);
    public TextField channelNameInput;
    public CheckBox autoConnectCheckBox;
    public Button cancelButton;
    public Button addButton;
    public Label headerLabel;
    public HBox channelIdHbox;
    public Label channelIdHintLabel;
    public TextField channelIdInput;
    public Button openBrowserButton;
    public Label statusLabel;
    public Label channelHintLabel;
    public Label channelNameLabel;
    private String platform;
    public KickChannel currentKickChannel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void cancelButtonClicked() {
        closeWindow();
    }

    public void addChannel() {

        String channelName = channelNameInput.getText();
        boolean autoConnect = autoConnectCheckBox.isSelected();
        boolean isTwitch = platform.equalsIgnoreCase("twitch");
        boolean isKick = platform.equalsIgnoreCase("kick");
        boolean isYoutube = platform.equalsIgnoreCase("youtube");
        LOGGER.info("Trying to add channel {} to {}",channelName,platform);
        if (channelName.equals("")) {
            LabelHandler.showStatusLabel(10,statusLabel,"Channel name cannot be blank.",true);
            return;
        }
        if (isTwitch) {

            Channel channel = new Channel(channelName, "twitch", autoConnect);
            boolean addSuccessful = StaticProperties.settings.addTwitchChannel(channel);
            if(addSuccessful) {

                closeWindow();
            } else {
                LabelHandler.showStatusLabel(10,statusLabel,"Could not add channel, please try again",true);
            }
        } else if (isKick) {
            int channelId;
            try {
                channelId = Integer.parseInt(channelIdInput.getText());

            } catch (NumberFormatException e) {
                LOGGER.warn("NumberFormatException: ", e);
                LabelHandler.showStatusLabel(10,statusLabel,"Channel id can only be numbers.",true);
                return;
            }
            KickChannel channel = new KickChannel(channelName, channelId, autoConnect);
            boolean addSuccessful = StaticProperties.settings.addKickChannel(channel);
            if(addSuccessful) {

                closeWindow();
            } else {
                LOGGER.warn("name already added");
                LabelHandler.showStatusLabel(10,statusLabel,"Channel name is already added.",true);
            }

        } else if (isYoutube) {
            if (StaticProperties.settings.getYoutubeChannel() == null) {
                StaticProperties.settings.setYoutubeChannel(new YoutubeChannel("null", channelName));
            } else {
                YoutubeChannel youtubeChannel = StaticProperties.settings.getYoutubeChannel();
                youtubeChannel.setVideoUrl(channelName);
                youtubeChannel.setChannelName("null");
                if (StaticProperties.youtube != null) StaticProperties.youtube.requestRestart();
                StaticProperties.settings.setYoutubeChannel(StaticProperties.settings.getYoutubeChannel());

            }
            closeWindow();
        }
    }
    public void updateChannel() {
        currentKickChannel.setChannelId(Integer.parseInt(channelIdInput.getText()));
        StaticProperties.settings.db.updateChannel(currentKickChannel);
        closeWindow();
    }


    public void addButtonClicked(ActionEvent actionEvent) {
        if (addButton.getText().equalsIgnoreCase("add")) addChannel();
        else if (addButton.getText().equalsIgnoreCase("update")) updateChannel();

    }
    public void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
    public void setPlatform(String platform) {
        this.platform = platform;
        headerLabel.setText(String.format("Add %s Channel", platform));
        if (!platform.equalsIgnoreCase("kick")) {
            channelIdHbox.setVisible(false);
            channelIdHbox.setManaged(false);
            channelIdHintLabel.setVisible(false);
            channelIdHintLabel.setManaged(false);
            openBrowserButton.setVisible(false);
            openBrowserButton.setManaged(false);
        } else {
            addButton.setDisable(true);
            openBrowserButton.setDisable(true);
        }
        if (platform.equalsIgnoreCase("youtube")) {
            autoConnectCheckBox.setVisible(false);
            autoConnectCheckBox.setManaged(false);
            channelHintLabel.setText("Enter the live YouTube channel URL.");
            channelNameLabel.setText("Video URL");

        }
    }

    public void channelNameInputTextChanged() {
        addButton.setDisable(!checkInputsForButton());
        openBrowserButton.setDisable(channelNameInput.getText().isEmpty());
    }

    public void openBrowserButtonClicked() {
        if (!channelNameInput.getText().isEmpty()) {
            String url = String.format("https://kick.com/api/v2/channels/%s/chatroom", channelNameInput.getText());
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void channelIdInputKeyTyped() {
        addButton.setDisable(!checkInputsForButton());
    }
    public boolean checkInputsForButton() {
        if (platform.equalsIgnoreCase("kick"))
            return (!channelNameInput.getText().isEmpty() && !channelIdInput.getText().isEmpty()); //returns true if both inputs have data
        else return true;
    }
}
