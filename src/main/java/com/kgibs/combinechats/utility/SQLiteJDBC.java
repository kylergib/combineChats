package com.kgibs.combinechats.utility;


import com.kgibs.combinechats.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SQLiteJDBC {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteJDBC.class);
    private String url;

    public SQLiteJDBC() {

    }

    public void initDatabase(String filePath) {

        url = "jdbc:sqlite:" + filePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                LOGGER.info("The driver name is " + meta.getDriverName());
                LOGGER.info("A new database has been created.");
                createTables(conn);
            }


        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }

    }
    public List<String> listTables() {
        List<String> tableNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tableNames.add(rs.getString("name"));
            }


        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return tableNames;
    }




    public void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        LOGGER.info("Creating tables if not found");
        String channelsTable = """
                CREATE TABLE IF NOT EXISTS channels (
                	id integer PRIMARY KEY,
                	name text NOT NULL,
                	platform text NOT NULL,
                	auto_connect integer NOT NULL,
                	chat_id integer,
                	url text UNIQUE
                );""";
        stmt.execute(channelsTable);

        String settingsTable = """
                CREATE TABLE IF NOT EXISTS settings (
                	id integer PRIMARY KEY,
                	kick_message_color text NOT NULL,
                	twitch_message_color text NOT NULL,
                	youtube_message_color text NOT NULL,
                	dark_mode_on integer NOT NULL,
                	show_channel_name_in_chat integer NOT NULL,
                	show_platform_name_in_chat integer NOT NULL,
                	notification_on_chat integer NOT NULL,
                	notification_sleep_duration integer NOT NULL
                );""";
        stmt.execute(settingsTable);

        String messagesTable = """
                CREATE TABLE IF NOT EXISTS messages (
                	id integer PRIMARY KEY,
                	platform text NOT NULL,
                	channel_name text NOT NULL,
                	user text NOT NULL,
                	message text NOT NULL,
                    time_received text NOT NULL
                );""";
        stmt.execute(messagesTable);


    }

    public int insertMessage(String platform, String channelName,String user, String message, LocalDateTime localDateTime) throws SQLException {
        if (platform == null || channelName == null || user == null ||
                message == null || localDateTime == null)
            return -1;
        try (Connection conn = DriverManager.getConnection(url)) {
            String localDateString = localDateTime.toString();
            String sqlInsert = "INSERT INTO messages(platform, channel_name, user, message,time_received) VALUES(?,?,?, ?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, platform);
                pstmtInsert.setString(2, channelName);
                pstmtInsert.setString(3, user);
                pstmtInsert.setString(4, message);
                pstmtInsert.setString(5, localDateString);
                pstmtInsert.executeUpdate();

                int id = -1;
                try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1);
                    }
                }
                return id;
            }

        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return -1;
    }
    public List<ChatMessage> findMessage(String platform, String channelName, String userName,
                                   String message, LocalDateTime startLocalDateTime,
                                   LocalDateTime endLocalDateTime, boolean exactMatch) throws SQLException {
        List<ChatMessage> foundMessages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url)) {

            String sql = "SELECT * from messages";
            List<String> paramsList = new ArrayList<>();
            List<String> paramsInject = new ArrayList<>();

            if (platform != null) {
                paramsList.add("LOWER(platform) = LOWER(?)");
                paramsInject.add(platform);

            }
            if (channelName != null) {
                paramsList.add("LOWER(channel_name) LIKE LOWER(?)");
                paramsInject.add("%" + channelName + "%");
            }
            if (userName != null) {
                paramsList.add("user = ?");
                paramsInject.add(userName);
            }
            if (message != null) {

                if (exactMatch) {
                    paramsList.add("LOWER(message) = LOWER(?)");
                    paramsInject.add(message);
                } else {
                    paramsList.add("LOWER(message) LIKE LOWER(?)");
                    paramsInject.add("%" + message + "%");
                }
            }
            if (startLocalDateTime != null && endLocalDateTime != null) {
                paramsList.add("time_received >= ?");
                paramsList.add("time_received <= ?");
                paramsInject.add(startLocalDateTime.toString());
                paramsInject.add(endLocalDateTime.toString());
            }
            if (paramsList.size() != paramsInject.size()) {
                LOGGER.warn("Message params does not match injection amount");
                return null;
            }

            if (paramsList.size() > 0) {
                String paramsString = " WHERE";
                for (int i = 0; i < paramsList.size(); i++) {
                    if (!paramsString.equalsIgnoreCase(" WHERE")) paramsString += (" AND " + paramsList.get(i));
                    else paramsString += (" " + paramsList.get(i));
                }
                sql += paramsString;
            }
            LOGGER.trace("SQL: {}",sql);
            paramsList.forEach(object->LOGGER.trace("List: {}",object.toString()));
            paramsInject.forEach(object->LOGGER.trace("Inject: {}",object.toString()));
            LOGGER.trace("Inject size: {}",String.valueOf(paramsInject.size()));
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < paramsInject.size(); i++) {
                    pstmt.setObject(i + 1,paramsInject.get(i));
                }

                LOGGER.trace("pstmt: {}", pstmt.toString());

                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String platformString = resultSet.getString("platform");
                    String channelNameString = resultSet.getString("channel_name");
                    String userString = resultSet.getString("user");
                    String messageString = resultSet.getString("message");
                    String time_received = resultSet.getString("time_received");
                    LocalDateTime localDateTime = LocalDateTime.parse(time_received);

                    foundMessages.add(new ChatMessage(platformString, channelNameString,userString, messageString,localDateTime));
                }
                resultSet.close();
            }
            return foundMessages;
        }


    }
    public List<MessageCount> testMessageCount(String platform, String channelName, String userName,
                                               String message, LocalDateTime startLocalDateTime,
                                               LocalDateTime endLocalDateTime, boolean exactMatch) {
        List<MessageCount> messageCountList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url)) {
            String query = "SELECT DATE(time_received) AS day, COUNT(*) AS count " +
                    "FROM messages " +
                    "WHERE time_received >= ? AND time_received <= ? " +
                    "GROUP BY day";
            PreparedStatement statement = conn.prepareStatement(query);
//            statement.setString(1, platform);
            statement.setString(1, String.valueOf(startLocalDateTime));
            statement.setString(2, String.valueOf(endLocalDateTime));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String day = resultSet.getString("day");
                LocalDate date = LocalDate.parse(day);
                int count = resultSet.getInt("count");
                messageCountList.add(new MessageCount(date.getDayOfMonth(),count));
                LOGGER.trace(day + ": " + count + " messages");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messageCountList;
    }

    public ChatMessage getMessage(int messageId) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * from messages where id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, messageId);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String platform = resultSet.getString("platform");
                    String channelName = resultSet.getString("channel_name");
                    String user = resultSet.getString("user");
                    String message = resultSet.getString("message");
                    String time_received = resultSet.getString("time_received");
                    LocalDateTime localDateTime = LocalDateTime.parse(time_received);

                    return new ChatMessage(platform, channelName,user, message,localDateTime);

                }
                resultSet.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return null;
    }

    public int updateSettings(Settings settings) {
        try (Connection connection = DriverManager.getConnection(url)) {
            String sqlUpdate = "UPDATE settings SET kick_message_color = ?, twitch_message_color = ?," +
                    "youtube_message_color = ?, dark_mode_on = ?," +
                    "show_channel_name_in_chat = ?, " +
                    "show_platform_name_in_chat = ?, notification_on_chat = ?," +
                    "notification_sleep_duration = ? WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {
                pstmt.setString(1, settings.getKickMessageColor());
                pstmt.setString(2, settings.getTwitchMessageColor());
                pstmt.setString(3, settings.getYoutubeMessageColor());
                pstmt.setInt(4, settings.getDarkModeOn() ? 1 : 0);
                pstmt.setInt(5, settings.isShowChannelNameInMessage() ? 1 : 0);
                pstmt.setInt(6, settings.isShowPlatformNameInMessage() ? 1 : 0);
                pstmt.setInt(7, settings.isNotificationOn() ? 1 : 0);
                pstmt.setInt(8, settings.getNotificationDuration());
                pstmt.setInt(9, 1);
                pstmt.executeUpdate();
                int id = -1;
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1);
                    }
                }
                LOGGER.info("Update successful! ID is {}", id);
                return id;


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;

    }

    public void insertSettings(Settings settings) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sqlInsert = "INSERT INTO settings(kick_message_color," +
                    "twitch_message_color,youtube_message_color," +
                    "dark_mode_on," +
                    "show_channel_name_in_chat,show_platform_name_in_chat," +
                    "notification_on_chat,notification_sleep_duration" +
                    ") VALUES(?,?,?,?,?,?,?,?)";

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, settings.getKickMessageColor());
                pstmtInsert.setString(2, settings.getTwitchMessageColor());
                pstmtInsert.setString(3, settings.getYoutubeMessageColor());
                pstmtInsert.setInt(4, settings.getDarkModeOn() ? 1 : 0);
                pstmtInsert.setInt(5, settings.isShowChannelNameInMessage() ? 1 : 0);
                pstmtInsert.setInt(6, settings.isShowPlatformNameInMessage() ? 1 : 0);
                pstmtInsert.setInt(7, settings.isNotificationOn() ? 1 : 0);
                pstmtInsert.setInt(8, settings.getNotificationDuration());
                pstmtInsert.executeUpdate();
                LOGGER.info("Inserted a new setting.");
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> settingsMap = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * from settings where id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, 1);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    settingsMap.put("notificationOn", resultSet.getInt("notification_on_chat") != 0);
                    settingsMap.put("notificationDuration", resultSet.getInt("notification_sleep_duration"));
                    settingsMap.put("kickMessageColor", resultSet.getString("kick_message_color"));
                    settingsMap.put("twitchMessageColor", resultSet.getString("twitch_message_color"));
                    settingsMap.put("youtubeMessageColor", resultSet.getString("youtube_message_color"));
                    settingsMap.put("darkModeOn", resultSet.getInt("dark_mode_on") != 0);
                    settingsMap.put("showChannelNameInMessage", resultSet.getInt("show_channel_name_in_chat") != 0);
                    settingsMap.put("showPlatformNameInMessage", resultSet.getInt("show_platform_name_in_chat") != 0);
                    if (id == 1) return settingsMap;
                    else return null;
                }
                resultSet.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        LOGGER.warn("No settings found");
        return null;

    }

    public List<Channel> getAllChannels() {
        List<Channel> allChannels = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * from channels";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet resultSet = pstmt.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String platform = resultSet.getString("platform");
                    Boolean autoConnect = resultSet.getInt("auto_connect") != 0;
                    int chatId = resultSet.getInt("chat_id");
                    String url = resultSet.getString("url");
                    Channel newChannel = null;
                    if (platform.equalsIgnoreCase("kick")) newChannel = new KickChannel(id,
                            name, chatId, autoConnect);
                    else if (platform.equalsIgnoreCase("youtube")) newChannel = new YoutubeChannel(id,
                            name, url);
                    else if (platform.equalsIgnoreCase("twitch")) newChannel = new Channel(id,
                            name, platform, autoConnect);

                    if (newChannel != null) allChannels.add(newChannel);

                }
                resultSet.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return allChannels;
    }

    public int insertChannel(Channel channel) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sqlInsert = "INSERT INTO channels(name," +
                    "platform, auto_connect, chat_id ,url" +
                    ") VALUES(?,?,?,?,?)";

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, channel.getChannelName());
                pstmtInsert.setString(2, channel.getPlatform());
                pstmtInsert.setInt(3, channel.isAutoConnect() ? 1 : 0);
                if (channel.getPlatform().equalsIgnoreCase("kick"))
                    pstmtInsert.setInt(4, ((KickChannel) channel).getChannelId());
                else pstmtInsert.setString(4, null);
                if (channel.getPlatform().equalsIgnoreCase("youtube"))
                    pstmtInsert.setString(5, ((YoutubeChannel) channel).getVideoUrl());
                else pstmtInsert.setString(5, null);
                pstmtInsert.executeUpdate();
                int id = -1;
                try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1);
                    }
                }
                LOGGER.info("Inserted a new channel: {} - channel ID: {}",channel.getChannelName(),id);
                return id;

            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return -1;
    }

    public void updateChannel(Channel channel) {
        try (Connection connection = DriverManager.getConnection(url)) {
            String platform = channel.getPlatform();
            boolean isTwitch = platform.equalsIgnoreCase("twitch");
            boolean isKick = platform.equalsIgnoreCase("kick");
            boolean isYoutube = platform.equalsIgnoreCase("youtube");
            String sqlUpdate = "UPDATE channels SET name = ?, platform = ?,auto_connect = ?";
            if (isKick) sqlUpdate += ",chat_id = ?";
            if (isYoutube) sqlUpdate += ",url = ?";
            sqlUpdate += " WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {

                pstmt.setString(1, channel.getChannelName());
                pstmt.setString(2, platform);
                pstmt.setInt(3, channel.isAutoConnect() ? 1 : 0);
                int idParamIndex = 4;
                if (platform.equalsIgnoreCase("kick")) {
                    int chatId = ((KickChannel) channel).getChannelId();
                    pstmt.setInt(4, chatId);
                    idParamIndex = 5;
                }

                if (platform.equalsIgnoreCase("youtube")) {
                    String url = ((YoutubeChannel) channel).getVideoUrl();
                    pstmt.setString(4, url);
                    idParamIndex = 5;
                }

                pstmt.setInt(idParamIndex, channel.getId());
                // update
                pstmt.executeUpdate();

                LOGGER.info("Update successful!");

            } catch (SQLException e) {
                LOGGER.warn("Exception has occurred: ",e);
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
    }

    public void removeChannel(int channelID) {
        LOGGER.info("trying to remove  - ID: {}",channelID);
        String sql = "DELETE FROM channels WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, channelID);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                LOGGER.warn("Exception has occurred: ",e);
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
    }
    public Channel getChannel(int channelId) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * from channels where id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, channelId);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String platform = resultSet.getString("platform");
                    boolean autoConnect = resultSet.getInt("auto_connect") == 1;
                    Channel channel = null;
                    if (platform.equalsIgnoreCase("kick")) {
                        int chatId = resultSet.getInt("chat_id");
                        channel = new KickChannel(id,name,chatId,autoConnect);
                    } else if (platform.equalsIgnoreCase("youtube")) {
                        String url = resultSet.getString("url");
                        channel = new YoutubeChannel(id,name,url);
                    } else {
                        channel = new Channel(id,name,"twitch",autoConnect);
                    }

                    return channel;

                }
                resultSet.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception has occurred: ",e);
        }
        return null;
    }
}