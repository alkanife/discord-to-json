package dev.alkanife.discordtojson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
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
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DiscordToJson {

    @Getter
    private final String github, build, version, fullVersion;
    @Getter @Setter
    private Parameters parameters;
    @Getter
    private JDA jda;
    @Getter @Setter
    private int times = 0;
    @Getter @Setter
    private Logger logger;
    @Getter @Setter
    private Guild guild;
    @Getter @Setter
    private TextChannel textChannel;
    @Getter
    private final List<DownloadedMessage> messageList = new ArrayList<>();

    public DiscordToJson(String[] args) {
        // Read build info
        github = "https://github.com/alkanife/discord-to-json";
        version = Utils.readResource("/version.txt");
        build = Utils.readResource("/build.txt");
        fullVersion = version + " (" + build + ")";

        // Parse args
        ParamParser paramParser = new ParamParser(this);
        if (!paramParser.parse(args))
            return;

        // Setup logger
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(root.getLoggerContext());
        consoleAppender.setEncoder(Utils.getPatternLayout(root, parameters.isDebug(), false));
        consoleAppender.start();
        root.addAppender(consoleAppender);

        if (parameters.getLogs() != null) {
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
            fileAppender.setContext(root.getLoggerContext());
            fileAppender.setEncoder(Utils.getPatternLayout(root, parameters.isDebug(), true));
            fileAppender.setAppend(true);
            fileAppender.setFile(parameters.getLogs());
            fileAppender.start();
            root.addAppender(fileAppender);
        }

        root.setLevel(parameters.isDebug() ? Level.DEBUG : Level.INFO);

        logger = (Logger) LoggerFactory.getLogger(DiscordToJson.class);
        logger.setLevel(parameters.isDebug() ? Level.DEBUG : Level.INFO);

        // splash
        logger.info("--------------------------");
        logger.info("discord-to-json version " + fullVersion);
        logger.info("Github: " + github);
        logger.info("--------------------------");

        logger.debug("Using params " + parameters.toString());

        // Start JDA
        try {
            logger.info("Connecting to Discord...");
            JDABuilder jdaBuilder = JDABuilder.createDefault(parameters.getToken());
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.IDLE);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
            jdaBuilder.addEventListeners(new EventListener(this));
            jda = jdaBuilder.build();
        } catch (Exception exception) {
            logger.error("Unexpected JDA error:", exception);
        }
    }

    public void end() {
        logger.info(messageList.size() + " messages have been downloaded.");

        if (parameters.getOutputFilePath() == null)
            parameters.setOutputFilePath(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".json");

        try {
            logger.debug("Building JSON");
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
            Type typeDate = new TypeToken<List<DownloadedMessage>>(){}.getType();
            String json = gson.toJson(messageList, typeDate);

            File outputFile = new File(parameters.getOutputFilePath());

            if (outputFile.exists() && !parameters.isReplaceOutputFile()) {
                String random = new Random().ints(97, 123).limit(5).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
                outputFile = new File(random + parameters.getOutputFilePath());
            }

            logger.info("Exporting to " + parameters.getOutputFilePath());
            Files.writeString(outputFile.toPath(), json);
        } catch (Exception exception) {
            logger.error("Failed to export messages to JSON...", exception);
        }

        jda.shutdown();
    }

    public void addMessage(int i, Message message) {
        logger.debug(" > " + i + "/" + parameters.getLimit() + ": '" + message.getId() + "' by '" + message.getAuthor().getName() + "'");
        messageList.add(new DownloadedMessage(message));
    }
}
