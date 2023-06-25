package dev.alkanife.discordtojson;

public class Fetcher {

    public Fetcher(DiscordToJson discordToJson, String startId) {
        discordToJson.setTimes(discordToJson.getTimes()+1);

        discordToJson.debug("Fetching - " + discordToJson.getTimes());

        discordToJson.getTextChannel().getHistoryAfter(startId, discordToJson.getParameters().getLimit()).queue(messageHistory -> {
            if (messageHistory == null) {
                discordToJson.debug("MessageHistory is null");
                discordToJson.end();
                return;
            }

            if (messageHistory.size() == 0) {
                discordToJson.debug("MessageHistory empty");
                discordToJson.end();
                return;
            }

            for (int i = messageHistory.size()-1; i >= 0; i--)
                discordToJson.insert(messageHistory.getRetrievedHistory().get(i));

            try {
                discordToJson.debug("Sleeping " + discordToJson.getParameters().getDelay() + " ms");
                Thread.sleep(discordToJson.getParameters().getDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Fetcher(discordToJson, messageHistory.getRetrievedHistory().get(0).getId());
        });
    }
}
