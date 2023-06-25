package dev.alkanife.discordtojson.json;

import lombok.Data;

@Data
public class DownloadedMessage {

    private String id, date;
    private Author author;
    private String content;

}
