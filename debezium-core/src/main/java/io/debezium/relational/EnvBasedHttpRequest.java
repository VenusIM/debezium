package io.debezium.relational;

import io.debezium.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class EnvBasedHttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvBasedHttpRequest.class);

    public void trigger(String tableName) {
        String triggerUrl = System.getenv("TRIGGER_URL");
        if (Strings.isNullOrEmpty(triggerUrl)) {
            LOGGER.warn("EnvBasedHttpRequest > Environment variable 'TRIGGER_URL' is not set or empty");
            return;
        }

        String user = System.getenv("JENKINS_USER");
        if (Strings.isNullOrEmpty(user)) {
            LOGGER.warn("EnvBasedHttpRequest > Environment variable 'JENKINS_USER' is not set or empty");
            return;
        }

        String token = System.getenv("JENKINS_TOKEN");
        if (Strings.isNullOrEmpty(token)) {
            LOGGER.warn("EnvBasedHttpRequest > Environment variable 'JENKINS_TOKEN' is not set or empty");
            return;
        }

        String url = triggerUrl + "&SCHEMA=" + getSchemaFromTable(tableName);
        try {
            String str = user+":"+token;
            String encodedStr = Base64.getEncoder().encodeToString(str.getBytes());
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encodedStr);

            int status = connection.getResponseCode();
            String message = connection.getResponseMessage();
            LOGGER.info("EnvBasedHttpRequest > HTTP response code: {}, HTTP message : {}", status, message);

            if(status != 201) {
                throw new RuntimeException(String.format("HTTP response code: %s HTTP message : %s", status, message));
            }

            connection.disconnect();
        } catch (Exception e) {
            LOGGER.error("EnvBasedHttpRequest > HTTP request failed", e);
        }
    }

    private String getSchemaFromTable(String tableName) {
        if (tableName.startsWith("aidt_lms")) {
            return "lms";
        } else if (tableName.startsWith("aidt_lcms")) {
            return "lcms";
        } else {
            return null;
        }
    }
}
