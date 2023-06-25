package dev.alkanife.discordtojson;

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

    }
}
