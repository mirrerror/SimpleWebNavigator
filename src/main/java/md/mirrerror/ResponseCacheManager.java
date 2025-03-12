package md.mirrerror;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResponseCacheManager {

    private static final String CACHE_FILE = "url_cache.txt";

    private final Map<String, String> urlCache = new HashMap<>();

    public void loadCache() throws IOException {
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    urlCache.put(parts[0], parts[1]);
                }
            }
            reader.close();
        }
    }

    public void saveToCache(String urlString, String response) throws IOException {
        File cacheFile = new File(CACHE_FILE);

        if (!cacheFile.exists()) {
            cacheFile.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE_FILE));

        for (Map.Entry<String, String> entry : urlCache.entrySet()) {
            writer.write(entry.getKey() + " " + entry.getValue() + "\n");
        }

        writer.write(urlString + " " + response + "\n");
        writer.close();
    }

    public String getFromCache(String urlString) {
        return urlCache.get(urlString);
    }

}
