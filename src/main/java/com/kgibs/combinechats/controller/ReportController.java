package com.kgibs.combinechats.controller;

import com.kgibs.combinechats.model.ChatMessage;
import com.kgibs.combinechats.model.MessageCount;
import com.kgibs.combinechats.utility.LabelHandler;
import com.kgibs.combinechats.utility.StaticProperties;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.paint.Color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);
    public VBox messageCountVbox;
    public VBox searchMessagesVbox;
    public TextField platformTextArea;
    public TextField messageTextArea;
    public TextField channelTextArea;
    public TextField userTextArea;
    public Button searchButton;
    public CheckBox exactMatchCheckbox;
    public DatePicker datePicker;
    public TableView filterTable;
    public TableColumn dateColumn;
    public TableColumn platformColumn;
    public TableColumn channelColumn;
    public TableColumn userColumn;
    public TableColumn messageColumn;
    public ChoiceBox monthChoiceBox;
    public TableColumn leftSideDateColumn;
    public TableColumn leftSideMessagesColumn;
    public TableColumn rightSideDateColumn;
    public TableColumn rightSideMessagesColumn;
    public TableView leftSideTable;
    public TableView rightSideTable;
    public MenuButton menuButton;
    public Button saveCountButton;
    public Label statusLabelCount;
    public Label statusLabelChatTable;
    private LocalDate currentDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageLookupMenuItemClicked();
        currentDate = LocalDate.now();
        getLastTwelveMonths();
        setTables();

    }
    public void getLastTwelveMonths() {
        ObservableList monthNames = FXCollections.observableArrayList();
        LocalDate localDate = LocalDate.now();
        LOGGER.trace("Todays date: {}", localDate.getMonthValue());
        monthNames.add(getMonthFromInt(localDate.getMonthValue()) + " " + localDate.getYear());
        for (int i = 1; i < 13; i++) {
            int monthNum = localDate.getMonthValue() - i;
            int year = localDate.getYear();
            if (monthNum < 0) {
                monthNum = 12 + monthNum;
                year = localDate.getYear() - 1;
            }
            LOGGER.trace("{} - {} - {}", monthNum, getMonthFromInt(monthNum), year);
            monthNames.add(getMonthFromInt(monthNum) + " " + year);

        }
        monthChoiceBox.setItems(monthNames);
        monthChoiceBox.setValue(getMonthFromInt(localDate.getMonthValue()) + " " + localDate.getYear());
        monthChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.trace("Old option: " + oldValue);
            LOGGER.trace("Selected option: " + newValue);
            String[] splitValue = newValue.toString().split(" ");
            int year = Integer.parseInt(splitValue[1]);
            String monthString = splitValue[0];
            currentDate = LocalDate.of(year,getIntFromMonth(monthString),1);
            setTables();
            LOGGER.trace("Selected date: {}", currentDate);
            setTables();
        });

    }
    public void setTables() {
        ObservableList<MessageCount> leftTableList = FXCollections.observableArrayList();
        ObservableList<MessageCount> rightTableList = FXCollections.observableArrayList();
        String platform = "twitch";
        String channelName = null;
        String userName = null;
        String messageString = null;
        boolean exactMatch = false;


        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        YearMonth yearMonthObject = YearMonth.of(year, month);
        int lastDayOfMonth = yearMonthObject.lengthOfMonth();
        LocalDateTime startLocalDateTime = LocalDateTime.of(year,month,1,0,0,0);
        LocalDateTime endLocalDateTime = LocalDateTime.of(year,month,lastDayOfMonth,23,59,59);
        List<MessageCount> messages = StaticProperties.settings.db.testMessageCount(platform,channelName,
                        userName,messageString,startLocalDateTime,endLocalDateTime,exactMatch);
        if (messages.size() > lastDayOfMonth) return;
        for (int i = 1; i <= lastDayOfMonth; i++) {
            MessageCount addMessage = null;
            for (MessageCount message : messages) {
                if (message.getDay() == i) {
                    addMessage = message;
                    break;
                }
            }
            if (addMessage == null) addMessage = new MessageCount(i,0);
            if (i <17)
                leftTableList.add(addMessage);
            else
                rightTableList.add(addMessage);
        }
        leftSideTable.setItems(leftTableList);
        rightSideTable.setItems(rightTableList);
        leftSideDateColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        leftSideMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        rightSideDateColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        rightSideMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("count"));







    }
    public int getIntFromMonth(String month) {
        return switch (month) {
            case "December" -> 12;
            case "January" -> 1;
            case "February" -> 2;
            case "March" -> 3;
            case "April" -> 4;
            case "May" -> 5;
            case "June" -> 6;
            case "July" -> 7;
            case "August" -> 8;
            case "September" -> 9;
            case "October" -> 10;
            case "November" -> 11;
            default -> -1;
        };
    }
    public String getMonthFromInt(int month) {
        return switch (month) {
            case 0, 12 -> "December";
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            default -> null;
        };
    }

    public void searchButtonClicked(ActionEvent actionEvent) {
        String platform = platformTextArea.getText();
        String channelName = channelTextArea.getText();
        String userName = userTextArea.getText();
        String messageString = messageTextArea.getText();
        boolean exactMatch = exactMatchCheckbox.isSelected();

        if (platform.equals("")) platform = null;
        if (channelName.equals("")) channelName = null;
        if (userName.equals("")) userName = null;
        if (messageString.equals("")) messageString = null;
        LocalDateTime startLocalDateTime = null;
        LocalDateTime endLocalDateTime = null;
        if (datePicker.getValue() != null) {
            startLocalDateTime = datePicker.getValue().atStartOfDay();
            endLocalDateTime = datePicker.getValue().atTime(23,59,59);
        }

        try {
            List<ChatMessage> foundMessages = StaticProperties.settings.db.findMessage(platform,channelName,
                    userName,messageString,startLocalDateTime,endLocalDateTime,exactMatch);
            LOGGER.trace("Found messages");
            foundMessages.forEach(message -> LOGGER.trace(message.getMessage()));
            filterTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            ObservableList observableMessageList = FXCollections.observableList(foundMessages);
            filterTable.setItems(observableMessageList);
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("timeReceived"));

            dateColumn.setCellFactory(column -> {
                TableCell<ChatMessage, LocalDateTime> cell = new TableCell<>() {

                    @Override
                    protected void updateItem(LocalDateTime date, boolean empty) {
                        super.updateItem(date, empty);
                        if (empty || date == null) {
                            setText(null);
                        } else {
                            setText(String.format("%d-%d-%d", date.getMonthValue(), date.getDayOfMonth(), date.getYear()));
                        }
                    }
                };
                return cell;
            });


            platformColumn.setCellValueFactory(new PropertyValueFactory<>("platform"));
            channelColumn.setCellValueFactory(new PropertyValueFactory<>("channelName"));
            messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));;
            userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));



            messageColumn.setCellFactory(column -> new TableCell<ChatMessage, String>() {
                private final TextFlow textFlow = new TextFlow();
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        Text text = new Text(item);
                        text.setWrappingWidth(messageColumn.getWidth());
                        if (StaticProperties.settings.getDarkModeOn()) {
                            text.setFill(Color.WHITE);
                        }
                        setGraphic(text);
                    }
                }
            });
        } catch (SQLException e) {
            LOGGER.warn("Exception occurred: ", e);
        }


    }

    public void messageLookupMenuItemClicked( ) {
        searchMessagesVbox.setVisible(true);
        searchMessagesVbox.setVisible(true);
        messageCountVbox.setVisible(false);
        messageCountVbox.setManaged(false);
    }

    public void chatCountMenuItemClicked( ) {
        searchMessagesVbox.setVisible(false);
        searchMessagesVbox.setVisible(false);
        messageCountVbox.setVisible(true);
        messageCountVbox.setManaged(true);
    }

    public void saveReportButtonClicked() {
            String savePath = chooseSaveLocation();
            if (savePath == null) {
                LabelHandler.showStatusLabel(10,statusLabelChatTable, "Save location was not selected.",true);
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
                writer.write("Date,Platform,Channel,User,Message");
                writer.newLine();
                for (Object chatMessage: filterTable.getItems()) {
                    if (chatMessage instanceof ChatMessage) {
                        String line = String.format("%s,%s,%s,%s,%s",
                                ((ChatMessage) chatMessage).getTimeReceived(),
                                ((ChatMessage) chatMessage).getPlatform(),
                                ((ChatMessage) chatMessage).getChannelName(),
                                ((ChatMessage) chatMessage).getUsername(),
                                ((ChatMessage) chatMessage).getMessage());
                        LOGGER.trace(line);
                        writer.write(line);
                        writer.newLine();
                    }

                }
                LabelHandler.showStatusLabel(10,statusLabelChatTable,
                        String.format("File saved successfully to: %s",savePath),false);

            } catch (IOException e) {
                LOGGER.warn("Exception occurred: ",e);
                LabelHandler.showStatusLabel(10,statusLabelChatTable, "Could not save the file. Please try again",true);
            }

    }
    public String chooseSaveLocation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated Values", "*.csv"));

        File saveLocation = fileChooser.showSaveDialog(menuButton.getScene().getWindow());
        if (saveLocation != null) {
            return saveLocation.getAbsolutePath();
        }
        return null;
    }

    public void saveCountButtonClicked() {

        String savePath = chooseSaveLocation();
        if (savePath == null) {
            LabelHandler.showStatusLabel(10,statusLabelCount, "Save location was not selected.",true);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
            writer.write("Date,Count");
            writer.newLine();
            addListToFile(writer,leftSideTable.getItems());
            addListToFile(writer,rightSideTable.getItems());
            LabelHandler.showStatusLabel(10,statusLabelCount,
                    String.format("File saved successfully to: %s",savePath),false);

        } catch (IOException e) {
            LOGGER.warn("Exception occurred: ",e);
            LabelHandler.showStatusLabel(10,statusLabelCount, "Could not save the file. Please try again",true);
        }
    }
    public void addListToFile(BufferedWriter writer, ObservableList addList) throws IOException {
        for (Object messageCount: addList) {
            if (messageCount instanceof MessageCount) {
                String line = String.format("%d-%d-%d,%d",
                        currentDate.getMonthValue(),((MessageCount) messageCount).getDay(),
                        currentDate.getYear(),((MessageCount) messageCount).getCount());
                LOGGER.trace(line);
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
