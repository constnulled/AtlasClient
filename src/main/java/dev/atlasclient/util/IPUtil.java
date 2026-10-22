package dev.atlasclient.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class IPUtil {
    public static IPInfo getIPInfo(String ip) {
        try {
            URL url = new URI("http://ip-api.com/json/" + (ip == null ? "" : ip)).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (json.has("status") && "success".equals(json.get("status").getAsString())) {
                    return new IPInfo(json);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class IPInfo {
        public String organisation;
        public String country;
        public String city;
        public String region;
        public String as;
        public String isp;
        public String timezone;
        public String ip;
        public String countryCode;

        public IPInfo(JsonObject json) {
            this.organisation = getOrEmpty(json, "org");
            this.country = getOrEmpty(json, "country");
            this.city = getOrEmpty(json, "city");
            this.region = getOrEmpty(json, "regionName");
            this.as = getOrEmpty(json, "as");
            this.isp = getOrEmpty(json, "isp");
            this.timezone = getOrEmpty(json, "timezone");
            this.ip = getOrEmpty(json, "query");
            this.countryCode = getOrEmpty(json, "countryCode");
        }

        private String getOrEmpty(JsonObject json, String key) {
            return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "Unknown";
        }
    }
}
