package com.kgibs.combinechats.utility;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public abstract class LabelHandler {

    public static void showStatusLabel(int duration, Label statusLabel, String statusMessage, boolean isError) {
        String style;
        if (isError && StaticProperties.settings.getDarkModeOn())
            style = "-fx-text-fill: #ca6363;";
        else if (isError && !StaticProperties.settings.getDarkModeOn()) {
            style = "-fx-text-fill: red;";
        } else if (!isError && StaticProperties.settings.getDarkModeOn()) {
            style = "-fx-text-fill: #c1ffc1;";
        } else
            style = "-fx-text-fill: green;";
        statusLabel.setStyle(style);
        statusLabel.setVisible(false);
        statusLabel.setText(statusMessage);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(duration), event -> statusLabel.setVisible(false))
        );
        timeline.setCycleCount(1);
        showLabelWithError(statusLabel, timeline);
    }
    private static void showLabelWithError(Label statusLabel, Timeline timeline) {
        statusLabel.setVisible(true);
        timeline.stop();
        timeline.play();
    }
}
