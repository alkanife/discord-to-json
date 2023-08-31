package dev.alkanife.discordtojson;

import com.beust.jcommander.Parameter;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Parameters {

    @Parameter(
            names = { "--help", "-h"},
            description = "Print usage"
    )
    @Getter
    @Setter
    private boolean help = false;

    @Parameter(
            names = { "--version", "-v" },
            description = "Print usage"
    )
    @Getter
    @Setter
    private boolean version = false;

    @Parameter(
            names = { "--debug", "--verbose", "-D"},
            description = "Debug/verbose mode"
    )
    @Getter
    @Setter
    private boolean debug = false;

    @Parameter(
            names = { "--token", "-t"},
            description = "Discord token"
    )
    @Getter
    @Setter
    @ToString.Exclude
    private String token = null;

    @Parameter(
            names = { "--tokenFile", "-tf"},
            description = "File containing the Discord token"
    )
    @Getter
    @Setter
    private String tokenFilePath = "token";

    @Parameter (
            names = { "--delay", "-d"},
            description = "The time between each download cycle"
    )
    @Getter
    @Setter
    private int delay = 5000;

    @Parameter (
            names = { "--limit", "-l"},
            description = "The number of messages downloaded each cycle"
    )
    @Getter
    @Setter
    private int limit = 70;

    @Parameter (
            names = { "--guild", "-g" },
            description = "The Discord guild id, only required if not using URL"
    )
    @Getter
    @Setter
    private String guildId = null;

    @Parameter (
            names = { "--channel", "-c" },
            description = "The Discord channel id, only required if not using URL"
    )
    @Getter
    @Setter
    private String channelId = null;

    @Parameter (
            names = { "--messageid", "-mid" },
            description = "The first message's id, only required if not using URL"
    )
    @Getter
    @Setter
    private String firstMessageId = null;

    @Parameter (
            names = { "--message", "-m" },
            description = "The first message's URL (https://discord.com/channels/guildId/channelId/messageId)"
    )
    @Getter
    @Setter
    private String firstMessageURL = null;

    @Parameter (
            names = { "--output", "-o" },
            description = "Output file name. Will use {date}.json by default"
    )
    @Getter
    @Setter
    private String outputFilePath = null;

    @Parameter (
            names = { "--replace", "-r" },
            description = "Replace output file if already existing"
    )
    @Getter
    @Setter
    private boolean replaceOutputFile = false;

    @Parameter (
            names = { "--cycles", "-C" },
            description = "The number of download cycles, < or = to 0 is infinite"
    )
    @Getter
    @Setter
    private int cycles = 0;

    @Parameter (
            names = { "--logs", "-L" },
            description = "Keep logs to the desired file path. Example: 'logs.txt'"
    )
    @Getter
    @Setter
    private String logs = null;

}
