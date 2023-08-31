package dev.alkanife.discordtojson;

import dev.alkanife.discordtojson.json.DownloadedMessage;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventListener extends ListenerAdapter {

    private final DiscordToJson discordToJson;

    public EventListener(DiscordToJson discordToJson) {
        this.discordToJson = discordToJson;
    }

    @Override
    public void onReady(@NotNull ReadyEvent readyEvent) {
        discordToJson.getLogger().info("Linking parameters with Discord...");

        Parameters param = discordToJson.getParameters();

        discordToJson.setGuild(readyEvent.getJDA().getGuildById(param.getGuildId()));

        if (discordToJson.getGuild() == null) {
            discordToJson.getLogger().error("Can't find guild with id '" + param.getGuildId() + "', shutting down");
            readyEvent.getJDA().shutdownNow();
            return;
        }

        discordToJson.setTextChannel(discordToJson.getGuild().getTextChannelById(param.getChannelId()));

        if (discordToJson.getTextChannel() == null) {
            discordToJson.getLogger().error("Can't find channel with id '" + param.getChannelId() + "', shutting down");
            readyEvent.getJDA().shutdownNow();
            return;
        }

        try {
            discordToJson.getTextChannel().retrieveMessageById(param.getFirstMessageId()).queue(
                    message -> {
                        if (message == null) {
                            discordToJson.getLogger().error("The first message is null! (id '" + param.getFirstMessageId() + "')");
                            discordToJson.getLogger().error("Shutting down");
                            readyEvent.getJDA().shutdown();
                            return;
                        }

                        discordToJson.getLogger().debug("Adding first message '" + message.getId() + "' by '" + message.getAuthor().getName() + "'");
                        discordToJson.getMessageList().add(new DownloadedMessage(message));

                        discordToJson.getLogger().info("Downloading " + discordToJson.getParameters().getLimit() + " messages every " + discordToJson.getParameters().getDelay() + " milliseconds...");

                        new Fetcher(discordToJson, message.getId());

                    }, throwable -> {
                        discordToJson.getLogger().error("An error occurred when trying to find the first message with id '" + param.getFirstMessageId() + "' (" + throwable + ")");
                        discordToJson.getLogger().error("Shutting down");
                        readyEvent.getJDA().shutdown();
                    }
            );
        } catch (Exception exception) {
            discordToJson.getLogger().error("Cannot find the first message with id '" + param.getFirstMessageId() + ", shutting down");
            readyEvent.getJDA().shutdown();
        }
    }
}
