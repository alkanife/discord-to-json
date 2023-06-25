package dev.alkanife.discordtojson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static String readResource(String file) {
        String string = "unknown";

        if (file != null) {
            try {
                InputStream inputStream = Utils.class.getResourceAsStream(file);
                if (inputStream != null)
                    string = new BufferedReader(new InputStreamReader(inputStream)).readLine();
            } catch (Exception ignore) {}
        }

        return string;
    }

}
