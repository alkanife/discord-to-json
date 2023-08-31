package dev.alkanife.discordtojson;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

    public static PatternLayoutEncoder getPatternLayout(Logger root, boolean debug, boolean file) {
        String LOG_CONSOLE_PATTERN = "%date{dd MMM HH:mm:ss} %gray(|) %boldGreen(%-15.-15logger{0}) %gray(|) %highlight(%-5.5level) %gray(|) %msg%n";
        String LOG_CONSOLE_DEBUG_PATTERN = "%date{dd MMM HH:mm:ss.SSS} %gray(|) %boldYellow(%thread) %gray(|) %boldGreen(%file:%line) %gray(|) %highlight(%level) %gray(|) %msg%n";
        String LOG_FILE_PATTERN = "%date{dd MMM HH:mm:ss.SSS} | %thread | %logger | %level | %msg%n";
        String LOG_FILE_DEBUG_PATTERN = "%date{dd MMM HH:mm:ss.SSS} | %thread | %file:%line | %level | %msg%n";

        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern(file ? (
                debug ? LOG_FILE_DEBUG_PATTERN : LOG_FILE_PATTERN
        ) : (
                debug ? LOG_CONSOLE_DEBUG_PATTERN : LOG_CONSOLE_PATTERN));
        patternLayoutEncoder.setContext(root.getLoggerContext());
        patternLayoutEncoder.setCharset(StandardCharsets.UTF_8);
        if (file)
            patternLayoutEncoder.setOutputPatternAsHeader(true);
        patternLayoutEncoder.start();

        return patternLayoutEncoder;
    }

}
