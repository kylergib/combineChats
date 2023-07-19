package com.kgibs.combinechats.utility;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TwitchParse {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchParse.class);
    public static JSONObject parseMessage(String message) {
        JSONObject parsedMessage = new JSONObject();

        int idx = 0;
        String rawTagsComponent = null;
        String rawSourceComponent = null;
        String rawCommandComponent = null;
        String rawParametersComponent = null;

        if (message.charAt(idx) == '@') {
            int endIdx = message.indexOf(' ');
            rawTagsComponent = message.substring(1, endIdx);
            idx = endIdx + 1;
        }

        if (message.charAt(idx) == ':') {
            idx += 1;
            int endIdx = message.indexOf(' ', idx);
            rawSourceComponent = message.substring(idx, endIdx);
            idx = endIdx + 1;
        }

        int endIdx = message.indexOf(':', idx);
        if (endIdx == -1) {
            endIdx = message.length();
        }
        rawCommandComponent = message.substring(idx, endIdx).trim();

        if (endIdx != message.length()) {
            idx = endIdx + 1;
            rawParametersComponent = message.substring(idx);
        }
        JSONObject commandJson = parseCommand(rawCommandComponent);
        if (commandJson.keySet().contains("command")) {
            String command = commandJson.getString("command");
            if (command.equals("null")) LOGGER.warn("Command is not found: {}",message);
            parsedMessage.put("command", command);
        }
        if (commandJson.keySet().contains("channel")) {
            String channel = commandJson.getString("channel");

            parsedMessage.put("channel", channel);
        }

        if (parsedMessage.get("command") == null) {
            return null;
        } else {

            if (rawTagsComponent != null) {
                parsedMessage.put("tags", parseTags(rawTagsComponent));
            }
            Source source = parseSource(rawSourceComponent);
            if (source != null) {
                parsedMessage.put("nick", source.nick);
                parsedMessage.put("host", source.host);
            }

            if (rawParametersComponent != null) {
                parsedMessage.put("content", parseParameters(rawParametersComponent));
            }
        }

        return parsedMessage;
    }

    private static JSONObject parseTags(String tags) {
        Set<String> tagsToIgnore = new HashSet<>(Arrays.asList("client-nonce", "flags"));

        JSONObject parsedTags = new JSONObject();
        for (String tag : tags.split(";")) {
            String[] parsedTag = tag.split("=");
            String tagValue = (parsedTag.length > 1 && !parsedTag[1].isEmpty()) ? parsedTag[1] : null;

            switch (parsedTag[0]) {
                case "badges":
                case "badge-info":

                    if (tagValue != null) {
                        Map<String, String> dict = new HashMap<>();
                        for (String badge : tagValue.split(",")) {
                            String[] badgeParts = badge.split("/");
                            dict.put(badgeParts[0], badgeParts[1]);
                        }
                        parsedTags.put(parsedTag[0], dict);
                    } else {
                        parsedTags.put(parsedTag[0], "null");
                    }
                    break;
                case "emotes":

                    if (tagValue != null) {
                        Map<String, List<Map<String, String>>> dictEmotes = new HashMap<>();
                        for (String emote : tagValue.split("/")) {
                            String[] emoteParts = emote.split(":");
                            List<Map<String, String>> textPositions = new ArrayList<>();

                            for (String position : emoteParts[1].split(",")) {
                                String[] positionParts = position.split("-");
                                Map<String, String> pos = new HashMap<>();
                                pos.put("startPosition", positionParts[0]);
                                pos.put("endPosition", positionParts[1]);
                                textPositions.add(pos);
                            }
                            dictEmotes.put(emoteParts[0], textPositions);
                        }
                        parsedTags.put(parsedTag[0], dictEmotes);
                    } else {
                        parsedTags.put(parsedTag[0], "null");
                    }
                    break;
                case "emote-sets":
                    parsedTags.put(parsedTag[0], Arrays.asList(tagValue.split(",")));
                    break;
                default:
                    if (!tagsToIgnore.contains(parsedTag[0])) {
                        parsedTags.put(parsedTag[0], tagValue);
                    }
            }
        }
        return parsedTags;
    }


    private static JSONObject parseCommand(String rawCommandComponent) {
        JSONObject parsedCommand = new JSONObject();
        String[] commandParts = rawCommandComponent.split(" ");


        switch (commandParts[0]) {
            case "JOIN":
                parsedCommand.put("command", commandParts[0]);
                parsedCommand.put("channel", commandParts[0]);
            case "PART":
            case "NOTICE":
            case "CLEARCHAT":
            case "HOSTTARGET":
            case "PRIVMSG":
                parsedCommand.put("command", commandParts[0]);
                parsedCommand.put("channel", commandParts[1]);
                break;
            case "PING":
                parsedCommand.put("command", commandParts[0]);
                break;
            case "PONG":
                parsedCommand.put("command", commandParts[0]);
                break;
            case "CAP":
                parsedCommand.put("command", commandParts[0]);
                parsedCommand.put("isCapRequestEnabled", "ACK".equals(commandParts[2]));
                break;
            case "GLOBALUSERSTATE":
                parsedCommand.put("command", commandParts[0]);
                break;
            case "USERSTATE":
            case "ROOMSTATE":
                parsedCommand.put("command", commandParts[0]);
                parsedCommand.put("channel", commandParts[1]);
                break;
            case "RECONNECT":
                LOGGER.warn("The Twitch IRC server is about to terminate the connection for maintenance.");
                parsedCommand.put("command", commandParts[0]);
                break;
            case "421":
                LOGGER.warn("Unsupported IRC command: " + commandParts[2]);
                return null;
            case "001":
                parsedCommand.put("command", commandParts[0]);
                parsedCommand.put("channel", commandParts[1]);
                break;
            case "002":
            case "003":
            case "004":
            case "353":
            case "366":
            case "372":
            case "375":
            case "376":
                LOGGER.warn("numeric message: " + commandParts[0]);
                parsedCommand.put("command", "null");
            case "USERNOTICE":
                LOGGER.warn("UserNotice: typically is for new subscription notifications for channel");
                parsedCommand.put("command", "null");
            default:
                LOGGER.warn("Unexpected command default: " + commandParts[0]);
                parsedCommand.put("command", "null");
        }

        return parsedCommand;

    }


    static class Source {
        String nick;
        String host;
    }

    public static Source parseSource(String rawSourceComponent) {
        if (rawSourceComponent == null) {
            return null;
        } else {
            String[] sourceParts = rawSourceComponent.split("!");
            Source source = new Source();

            if (sourceParts.length == 2) {
                source.nick = sourceParts[0];
                source.host = sourceParts[1];
            } else {
                source.nick = null;
                source.host = sourceParts[0];
            }
            return source;
        }
    }

    public static String parseParameters(String rawParametersComponent) {
        int idx = 0;
        return rawParametersComponent.substring(idx).trim();

    }
}






