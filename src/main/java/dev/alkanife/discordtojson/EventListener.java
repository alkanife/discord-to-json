package dev.alkanife.discordtojson;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
        discordToJson.print("Connected");
        discordToJson.print("Linking parameters with Discord...");

        Parameters param = discordToJson.getParameters();

        discordToJson.setGuild(readyEvent.getJDA().getGuildById(param.getGuildId()));

        if (discordToJson.getGuild() == null) {
            discordToJson.print("Can't find guild with id " + param.getGuildId());
            readyEvent.getJDA().shutdownNow();
            return;
        }

        discordToJson.setTextChannel(discordToJson.getGuild().getTextChannelById(param.getChannelId()));

        if (discordToJson.getTextChannel() == null) {
            discordToJson.print("Can't find channel with id " + param.getChannelId());
            readyEvent.getJDA().shutdownNow();
            return;
        }

        discordToJson.getTextChannel().retrieveMessageById(param.getFirstMessageId()).queue(message -> {
            if (message == null) {
                discordToJson.print("First message not found with id " + param.getFirstMessageId());
                readyEvent.getJDA().shutdown();
                return;
            }

            discordToJson.insert(message);

            discordToJson.print("Downloading...");

            new Fetcher(discordToJson, message.getId());
        });
    }
}
