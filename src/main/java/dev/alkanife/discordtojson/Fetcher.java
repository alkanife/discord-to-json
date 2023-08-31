package dev.alkanife.discordtojson;

public class Fetcher {

    public Fetcher(DiscordToJson discordToJson, String startId) {
        discordToJson.setTimes(discordToJson.getTimes()+1);

        discordToJson.getLogger().debug("[" + discordToJson.getTimes() + "] Fetching " + discordToJson.getParameters().getLimit() + " messages (already fetched: " + discordToJson.getMessageList().size() + ")");

        discordToJson.getTextChannel().getHistoryAfter(startId, discordToJson.getParameters().getLimit()).queue(messageHistory -> {
            if (messageHistory == null) {
                discordToJson.getLogger().debug("MessageHistory is null, ending");
                discordToJson.end();
                return;
            }

            if (messageHistory.isEmpty()) {
                discordToJson.getLogger().debug("MessageHistory empty, ending");
                discordToJson.end();
                return;
            }

            int messageIndex = 1;
            for (int i = messageHistory.size()-1; i >= 0; i--) {
                discordToJson.addMessage(messageIndex, messageHistory.getRetrievedHistory().get(i));
                messageIndex++;
            }

            if (discordToJson.getParameters().getCycles() != 0) {
                if (discordToJson.getTimes() >= discordToJson.getParameters().getCycles()) {
                    discordToJson.getLogger().debug("Cycle limit reached " + discordToJson.getTimes() + "/" + discordToJson.getParameters().getCycles());
                    discordToJson.end();
                    return;
                }
            }

            try {
                discordToJson.getLogger().debug("Sleeping " + discordToJson.getParameters().getDelay() + " ms");
                Thread.sleep(discordToJson.getParameters().getDelay());
            } catch (InterruptedException e) {
                discordToJson.getLogger().error("Something went wrong with the main thread; ", e);
            }

            new Fetcher(discordToJson, messageHistory.getRetrievedHistory().get(0).getId());
        });
    }
}
