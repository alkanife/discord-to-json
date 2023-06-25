package dev.alkanife.discordtojson;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.alkanife.discordtojson.json.Author;
import dev.alkanife.discordtojson.json.DownloadedMessage;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DiscordToJson {

    @Getter
    private final Parameters parameters;
    @Getter
    private JDA jda;
    @Getter @Setter
    private int times, messages = 0;
    @Getter @Setter
    private Logger logger;
    @Getter @Setter
    private Guild guild;
    @Getter @Setter
    private TextChannel textChannel;

    public DiscordToJson(String[] args) {
        parameters = new Parameters();
        JCommander jCommander = JCommander.newBuilder().programName("discord-to-json").addObject(parameters).build();

        try {
            jCommander.parse(args);
        } catch (ParameterException exception) {
            print("Invalid arguments, see correct usage with '--help'");
            return;
        }

        debug("Provided parameters: " + parameters.toString());

        if (parameters.isHelp() || args.length == 0) {
            jCommander.usage();
            return;
        }

        debug("Reading build info");

        String version = Utils.readResource("/version.txt");
        String build = Utils.readResource("/build.txt");
        String fullVersion = version + " (" + build + ")";

        if (parameters.isVersion()) {
            print("discord-to-json version " + version);
            print("Build date: " + fullVersion);
            print("https://github.com/alkanife/discord-to-json");
            return;
        }

        debug("Checking parameters");
        if (parameters.getToken() == null) {
            print("Invalid Discord token (null)");
            return;
        }

        if (parameters.getDelay() < 1) {
            print("Delay cannot be < to 1");
            return;
        }

        if (parameters.getLimit() < 1 || parameters.getLimit() > 100) {
            print("Limit cannot be < to 1 or > to 100. See https://javadoc.io/doc/net.dv8tion/JDA/latest/net/dv8tion/jda/api/entities/channel/middleman/MessageChannel.html#getHistoryAfter(long,int)");
            return;
        }

        if (parameters.getFirstMessageURL() != null) {
            if (parameters.getFirstMessageURL().contains("@me")) {
                print("Cannot read private messages.");
                return;
            }

            try {
                String[] urlArgs = parameters.getFirstMessageURL().replaceAll("https://discord.com/channels/", "").split("/");

                parameters.setGuildId(urlArgs[0]);
                parameters.setChannelId(urlArgs[1]);
                parameters.setFirstMessageId(urlArgs[2]);

            } catch (Exception exception) {
                print("Invalid message URL, please follow the https://discord.com/channels/guildId/channelId/messageId pattern");
                return;
            }
        }

        if (parameters.getGuildId() == null) {
            print("Invalid guild ID (null)");
            return;
        }

        if (parameters.getChannelId() == null) {
            print("Invalid channel ID (null)");
            return;
        }

        if (parameters.getFirstMessageId() == null) {
            print("Invalid first message ID (null)");
            return;
        }

        debug("Using " + parameters.toString());

        debug("Creating logger");
        logger = LoggerFactory.getLogger(DiscordToJson.class);

        print("discord-to-json version " + fullVersion);
        print("https://github.com/alkanife/discord-to-json");

        try {
            print("Connecting to Discord...");
            JDABuilder jdaBuilder = JDABuilder.createDefault(parameters.getToken());
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.IDLE);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
            jdaBuilder.addEventListeners(new EventListener(this));
            jda = jdaBuilder.build();
        } catch (Exception exception) {
            print("JDA error");
            exception.printStackTrace();
        }
    }

    public void print(String message) {
        if (logger == null)
            System.out.println(message);
        else
            logger.info(message);
    }

    public void debug(String message) {
        if (parameters.isDebug())
            print("[debug]: " + message);
    }

    private List<DownloadedMessage> messageList = new ArrayList<>();

    public void end() {
        print("Done! " + messages + " messages have been downloaded");

        if (parameters.getOutputFilePath() == null)
            parameters.setOutputFilePath(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".json");

        try {
            debug("Building JSON");
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
            Type typeDate = new TypeToken<List<DownloadedMessage>>(){}.getType();
            String json = gson.toJson(messageList, typeDate);

            File outputFile = new File(parameters.getOutputFilePath());

            if (outputFile.exists() && !parameters.isReplaceOutputFile()) {
                String random = new Random().ints(97, 123).limit(5).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
                outputFile = new File(random + parameters.getOutputFilePath());
            }

            print("Exporting to " + parameters.getOutputFilePath());
            Files.writeString(outputFile.toPath(), json);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        jda.shutdown();
    }

    public void insert(Message message) {
        messages++;

        debug("Adding " + message.getId() + " by " + message.getAuthor().getName() + " (" + messages + ")");

        DownloadedMessage downloadedMessage = new DownloadedMessage();
        downloadedMessage.setId(message.getId());
        downloadedMessage.setDate(message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
        downloadedMessage.setAuthor(new Author(message.getAuthor().getId(), message.getAuthor().getName()));
        downloadedMessage.setContent(message.getContentDisplay());

        messageList.add(downloadedMessage);
    }
}
