package com.uptake.log4j.slack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;

/**
 *
 */
public class SlackManager extends AbstractManager {

    private static final SlackManagerFactory FACTORY = new SlackManagerFactory();

    private final URL webhook;
    private final JsonFactory factory = new JsonFactory();

    private SlackManager(final LoggerContext loggerContext, final String name, final URL webhook) {
        super(loggerContext, name);
        this.webhook = webhook;
    }

    public void sendMessage(final String message) {
        try {
            HttpURLConnection connection = (HttpURLConnection) webhook.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/json");
            connection.connect();
            try (JsonGenerator generator = factory.createGenerator(connection.getOutputStream())) {
                generator.writeStartObject();
                generator.writeStringField("text", message);
                generator.writeEndObject();
            }
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new AppenderLoggingException("Got a non-200 status: " + responseCode);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SlackManager getManager(final String name, final LoggerContext context, final URL webhook) {
        SlackConfiguration config = new SlackConfiguration();
        config.context = context;
        config.webhook = webhook;
        return getManager(name, FACTORY, config);
    }

    private static class SlackConfiguration {
        private LoggerContext context;
        private URL webhook;
    }

    private static class SlackManagerFactory implements ManagerFactory<SlackManager, SlackConfiguration> {

        @Override
        public SlackManager createManager(final String name, final SlackConfiguration data) {
            return new SlackManager(data.context, name, data.webhook);
        }
    }
}
