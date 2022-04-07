package fr.alkanife.discorddownloader;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscordDownloader extends ListenerAdapter {

    private static boolean debug = false;
    private static int times = 0;
    private static int messages = 0;

    private static long delay = 5000;
    private static int limit = 70;

    private static String channelID;
    private static String firstMessageID;
    private static String filePath;

    private static JDA jda;

    public static void main(String[] args) {
        if (args.length < 3) {
            error("Arguments: <token> <channel-id> <firstmessage-id> [-debug]");
            return;
        }

        if (args[3] != null)
            if (args[3].equalsIgnoreCase("-debug"))
                debug = true;

        System.out.print("\033[H\033[2J");
        System.out.flush();

        try {
            log("Discord fetcher 1.2");
            log("-------------------");
            debug("Debug = true");
            debug("Delay: " + delay);
            debug("Messages/delay: " + limit);

            channelID = args[1];
            debug("Channel ID: " + channelID);

            firstMessageID = args[2];
            debug("First Message ID: " + firstMessageID);

            filePath = Paths.get("").toAbsolutePath().toString() + "/data.db";

            if (!(new File(filePath).exists())) {
                error("The data.db file was not found");
                return;
            }

            log("Connecting to Discord");

            JDABuilder jdaBuilder = JDABuilder.createDefault(args[0]);
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.IDLE);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES);
            jdaBuilder.addEventListeners(new DiscordDownloader());
            jda = jdaBuilder.build();
        } catch (Exception exception) {
            error("Error!");
            exception.printStackTrace();
        }
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        log("Connected");
        log("Retrieving messages in 5 seconds.");

        try {
            Thread.sleep(5000);

            log("Started! (may take a moment)");

            debug("Getting TextChannel");
            TextChannel textChannel = readyEvent.getJDA().getTextChannelById(channelID);

            if (textChannel == null) {
                error("TextChannel not found");
                jda.shutdown();
                return;
            }

            debug("TextChannel found: " + textChannel.getName() + " in " + textChannel.getGuild().getName());

            debug("Getting first message");
            textChannel.retrieveMessageById(firstMessageID).queue(message -> {
                if (message == null) {
                    error("First message not found");
                    jda.shutdown();
                    return;
                }

                debug("First message found: by " + message.getAuthor().getName());

                debug("Inserting first message");
                insert(message);
            });

            debug("Sleeping one second");
            Thread.sleep(1000);

            debug("Start process");
            retreive(textChannel, firstMessageID);
        } catch (Exception exception) {
            error("Error!");
            exception.printStackTrace();
        }
    }

    public void retreive(TextChannel textChannel, String id) {
        times++;

        debug("------------------------ RETRIEVE " + times + " ------------------------");

        textChannel.getHistoryAfter(id, limit).queue(messageHistory -> {
            if (messageHistory == null) {
                debug("MessageHistory = null");
                done();
                return;
            }

            if (messageHistory.size() == 0) {
                debug("MessageHistory empty");
                done();
                return;
            }

            for (int i = messageHistory.size()-1; i >= 0; i--)
                insert(messageHistory.getRetrievedHistory().get(i));

            try {
                debug("Sleeping " + delay + " ms");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                error("Error!");
                e.printStackTrace();
            }

            retreive(textChannel, messageHistory.getRetrievedHistory().get(0).getId());
        });
    }

    public void insert(Message message) {
        messages++;

        Connection connection = null;
        try {
            debug("Inserting message " + message.getId() + " by " + message.getAuthor().getName());

            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(false);

            //debug("getting connection");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);

            //debug("building statement");
            String sql = "INSERT INTO messages(message_id, author_id, date, content) VALUES(?,?,?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, message.getId());
            preparedStatement.setString(2, message.getAuthor().getId());
            preparedStatement.setString(3, message.getTimeCreated().toString());
            preparedStatement.setString(4, message.getContentDisplay());

            //debug("executing update");
            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            error("Error!");
            exception.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException exception) {
                error("Error!");
                exception.printStackTrace();
            }
        }
    }

    public static void done() {
        success("Done! " + messages + " messages have been inserted in the database.");
        jda.shutdown();
    }

    public static void log(String message) {
        log("\033[0;36m", message);
    }

    public static void log(String color, String message) {
        System.out.println(color + new SimpleDateFormat("hh:mm:ss aaa").format(new Date()) + "- " + message + "\033[0m");
    }

    public static void debug(String message) {
        if (debug)
            log("\033[0;33m", message);
    }

    public static void error(String message) {
        log("\033[0;31m", message);
    }

    public static void success(String message) {
        log("\033[0;32m", message);
    }

}

