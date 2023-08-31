package dev.alkanife.discordtojson;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.nio.file.Files;

public class ParamParser {

    private final DiscordToJson discordToJson;

    public ParamParser(DiscordToJson discordToJson) {
        this.discordToJson = discordToJson;
    }

    public boolean parse(String[] args) {
        Parameters parameters = new Parameters();
        JCommander jCommander = JCommander.newBuilder().programName("discord-to-json").addObject(parameters).build();

        try {
            jCommander.parse(args);
        } catch (ParameterException exception) {
            System.out.println("Invalid arguments, see correct usage with '--help'");
            return false;
        }

        if (parameters.isHelp() || args.length == 0) {
            jCommander.usage();
            return false;
        }

        if (parameters.isVersion()) {
            System.out.println("This is discord-to-json version " + discordToJson.getVersion());
            System.out.println("Build date: " + discordToJson.getBuild());
            System.out.println("GitHub: " + discordToJson.getGithub());
            System.out.println("See usage with --help");
            return false;
        }

        if (parameters.getToken() == null) {
            if (parameters.getTokenFilePath() == null) {
                System.out.println("Error, no Discord token provided!");
                return false;
            }

            File discordTokenFile = new File(parameters.getTokenFilePath());

            if (!discordTokenFile.exists()) {
                System.out.println("Discord token file not found!");
                return false;
            }

            try {
                parameters.setToken(Files.readString(discordTokenFile.toPath()));
            } catch (Exception e) {
                System.out.println("Failed to read the token file, please verify the content, or the file path");
                return false;
            }
        }

        if (parameters.getDelay() < 1) {
            System.out.println("Error, the delay cannot be < to 1");
            return false;
        }

        if (parameters.getLimit() < 1 || parameters.getLimit() > 100) {
            System.out.println("The limit cannot be < to 1 or > to 100. See https://javadoc.io/doc/net.dv8tion/JDA/latest/net/dv8tion/jda/api/entities/channel/middleman/MessageChannel.html#getHistoryAfter(long,int)");
            return false;
        }

        if (parameters.getCycles() < 0)
            parameters.setCycles(0);

        if (parameters.getFirstMessageURL() != null) {
            if (parameters.getFirstMessageURL().contains("@me")) {
                System.out.println("Cannot read private messages.");
                return false;
            }

            try {
                String[] urlArgs = parameters.getFirstMessageURL().replaceAll("https://discord.com/channels/", "").split("/");

                parameters.setGuildId(urlArgs[0]);
                parameters.setChannelId(urlArgs[1]);
                parameters.setFirstMessageId(urlArgs[2]);

            } catch (Exception exception) {
                System.out.println("Invalid message URL, please follow the 'https://discord.com/channels/guildId/channelId/messageId' pattern");
                return false;
            }
        }

        if (parameters.getGuildId() == null) {
            System.out.println("Error, no guild id was found");
            return false;
        }

        if (parameters.getChannelId() == null) {
            System.out.println("Error, no channel id was found");
            return false;
        }

        if (parameters.getFirstMessageId() == null) {
            System.out.println("Error, no message id was found");
            return false;
        }

        discordToJson.setParameters(parameters);
        return true;
    }

}
