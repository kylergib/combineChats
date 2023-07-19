package com.kgibs.combinechats.controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static com.kgibs.combinechats.utility.StaticProperties.*;

public class SettingsController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);
    public ColorPicker youtubeColorPicker;
    public ColorPicker kickColorPicker;
    public CheckBox showChannelNameCheckBox;
    public CheckBox showPlatformCheckbox;
    public ColorPicker twitchColorPicker;
    public Button cancelButton;
    public CheckBox darkModeCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        youtubeColorPicker.setValue(Color.web(settings.getYoutubeMessageColor()));
        kickColorPicker.setValue(Color.web(settings.getKickMessageColor()));
        twitchColorPicker.setValue(Color.web(settings.getTwitchMessageColor()));
        darkModeCheckBox.setSelected(settings.getDarkModeOn());
        showChannelNameCheckBox.setSelected(settings.isShowChannelNameInMessage());
        showPlatformCheckbox.setSelected(settings.isShowPlatformNameInMessage());
    }

    public void cancelButtonClicked() {
        closeWindow();
    }
    public void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void saveButtonClicked() {
        LOGGER.info("Updating settings:");
        LOGGER.info("Youtube color: {}",toHexString(youtubeColorPicker.getValue()));
        LOGGER.info("Kick color: {}",toHexString(kickColorPicker.getValue()));
        LOGGER.info("Twitch color: {}",toHexString(twitchColorPicker.getValue()));
        LOGGER.info("Dark Mode on: {}", darkModeCheckBox.isSelected());
        LOGGER.info("Show platform in messages: {}", showPlatformCheckbox.isSelected());
        LOGGER.info("Show channel in messages: {}", showChannelNameCheckBox.isSelected());

        settings.setYoutubeMessageColor(toHexString(youtubeColorPicker.getValue()));
        settings.setKickMessageColor(toHexString(kickColorPicker.getValue()));
        settings.setTwitchMessageColor(toHexString(twitchColorPicker.getValue()));
        settings.setDarkModeOn(darkModeCheckBox.isSelected());
        settings.setShowChannelNameInMessage(showChannelNameCheckBox.isSelected());
        settings.setShowPlatformNameInMessage(showPlatformCheckbox.isSelected());
        settings.updateSettings();

        closeWindow();




    }
    private String toHexString(Color c) {
        return String.format( "#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255) );
    }


}
