package md.mirrerror;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResponseCacheManager {

    private static final String CACHE_FILE = "url_cache.json";
    private final Map<String, String> urlCache = new HashMap<>();

    public void loadCache() throws IOException {
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
            JsonElement jsonElement = JsonParser.parseReader(reader);
            reader.close();

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    urlCache.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }
    }

    public void saveToCache(String urlString, String response) throws IOException {
        File cacheFile = new File(CACHE_FILE);
        JsonObject jsonObject;

        if (cacheFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
            JsonElement jsonElement = JsonParser.parseReader(reader);
            reader.close();

            if (jsonElement.isJsonObject()) {
                jsonObject = jsonElement.getAsJsonObject();
            } else {
                jsonObject = new JsonObject();
            }
        } else {
            jsonObject = new JsonObject();
        }

        jsonObject.addProperty(urlString, response);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE_FILE));
        writer.write(gson.toJson(jsonObject));
        writer.close();
    }

    public String getFromCache(String urlString) {
        return urlCache.get(urlString);
    }

}
