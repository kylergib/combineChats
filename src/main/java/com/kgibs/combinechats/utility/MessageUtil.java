package com.kgibs.combinechats.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.kgibs.combinechats.utility.StaticProperties.*;

public abstract class MessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);
    public static String parseMessage(String platform,String username, String channel,String messageContent) {
        messageContent = messageContent.replace("'","\'");
        String style = "";
        if (platform.equalsIgnoreCase("kick")) style = getStyle(settings.getKickMessageColor());
        else if (platform.equalsIgnoreCase("twitch")) style = getStyle(settings.getTwitchMessageColor());
        List<String> allSpans = new ArrayList<>();

        LOGGER.debug("Message settings: ");
        LOGGER.debug("Show Channel: {}, Show Platform: {}",settings.isShowChannelNameInMessage(),settings.isShowPlatformNameInMessage());
        if (settings.isShowChannelNameInMessage() && settings.isShowPlatformNameInMessage()) {
            allSpans.add(String.format("<span %s >(%s - %s)</span>",style,platform,channel));
        } else if (settings.isShowChannelNameInMessage()) {
            allSpans.add(String.format("<span %s >(%s)</span>",style,channel));
        } else if (settings.isShowPlatformNameInMessage()) {
            allSpans.add(String.format("<span %s >(%s)</span>",style,platform));
        }


        allSpans.add(String.format("<span %s >%s:</span>",style,username));

        List<String> emoteSpans = new ArrayList<>();
        if (messageContent.contains("[emote")) {
            String newMessage = "";
            List<String> splitString = List.of(messageContent.split(" "));
            List<Integer> emoteIndexList = new ArrayList<>();
            for (int i = 0; i < splitString.size(); i++) {
                String str = splitString.get(i);
                if (!newMessage.equals("")) newMessage += " ";
                if (str.length() > 0 && str.charAt(0) == '[' && str.substring(str.length() - 1, str.length()).equals("]")) {
                    List<String> imageString = List.of(str.split(":"));
                    String imageId = imageString.get(1).replace("]","");
                    String imageUrl = "";
                    if (platform.equalsIgnoreCase("kick")) {
                        imageUrl = String.format("https://files.kick.com/emotes/%s/fullsize",imageId);

                    } else if (platform.equalsIgnoreCase("twitch")) {
                        imageUrl = String.format("https://static-cdn.jtvnw.net/emoticons/v2/%s/default/light/1.0",imageId);
                    }
                    String span = String.format("<span><img src=\"%s\" style=\"height: 1.5em;\"></span>",imageUrl);

                    emoteSpans.add(span);
                    emoteIndexList.add(i);
                }


            }
            int startingIndex = 0;
            int lastIndex;
            for (int i = 0; i < emoteIndexList.size(); i++) {
                if (startingIndex < emoteIndexList.get(i)) {
                    String span = "";


                    for (int z = startingIndex; z < emoteIndexList.get(i); z++) {
                        if (!span.equals("")) span += " ";
                        span += splitString.get(z);
                    }
                    span = span.replace("\n","");
                    if (span.startsWith("https://")) {
                        allSpans.add(String.format("<span><a href=\"%s\">%s</a></span>",span,span));
                    } else {
                        allSpans.add(String.format("<span %s >%s</span>",style,span));
                    }

                    allSpans.add(emoteSpans.get(i));
                    startingIndex = emoteIndexList.get(i) + 1;
                } else if (emoteIndexList.get(i) == startingIndex) {
                    allSpans.add(emoteSpans.get(i));
                    startingIndex += 1;
                }


            }
        } else {
            String addContent = messageContent.replace("\n","");
            if (addContent.length() > 8 && addContent.trim().startsWith("https://")) {
                allSpans.add(String.format("<span><a href=\"%s\">%s</a></span>",addContent,addContent));
            } else {
                allSpans.add(String.format("<span %s >%s</span>",style,addContent));
            }


        }
        final String[] finalSpan = {"<div class=\"chatMessage\" style=\"padding: 5px\">"};
        allSpans.forEach(span -> {
            finalSpan[0] += span;
        });
        finalSpan[0] += "</div>";
        LOGGER.trace("{} - {}: {}", LocalDateTime.now(),username,messageContent);
        try {
            settings.db.insertMessage(platform,channel,username,messageContent,LocalDateTime.now());
            LOGGER.debug("Added new message from: {}", username);
        } catch (SQLException e) {
            LOGGER.warn("Exception occurred: ",e);
        }
        return String.format("document.body.innerHTML += '%s'", finalSpan[0]);

    }
}
