package dev.alkanife.discordtojson.json;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;

import java.time.format.DateTimeFormatter;

@Data
public class DownloadedMessage {

    private String id, date;
    private Author author;
    private String content;

    public DownloadedMessage(Message message) {
        id = message.getId();
        date = message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ"));
        author = new Author(message.getAuthor().getId(), message.getAuthor().getName());
        content = message.getContentDisplay();
    }

}
