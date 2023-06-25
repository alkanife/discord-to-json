package dev.alkanife.discordtojson;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordToJson {

    @Getter
    private final Parameters parameters;
    private JCommander jCommander;
    private String build, version, fullVersion;
    @Getter
    private JDA jda;
    @Getter @Setter
    private int times, messages = 0;

    public DiscordToJson(String[] args) {
        parameters = new Parameters();
        jCommander = JCommander.newBuilder().programName("discord-to-json").addObject(parameters).build();

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

        version = Utils.readResource("/version.txt");
        build = Utils.readResource("/build.txt");
        fullVersion = version + " (" + build + ")";

        if (parameters.isVersion()) {
            print("discord-to-json version " + version);
            print("Build date: " + fullVersion);
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

        print("Connecting to Discord");

        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(parameters.getToken());
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.IDLE);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES);
            jdaBuilder.addEventListeners(new EventListener(this));
            jda = jdaBuilder.build();
        } catch (Exception exception) {
            print("JDA error");
            exception.printStackTrace();
        }
    }

    public void print(String message) {
        System.out.println(message);
    }

    public void debug(String message) {
        //if (parameters.isDebug())
            print("[debug]: " + message);
    }





















}
