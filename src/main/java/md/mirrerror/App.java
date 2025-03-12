package md.mirrerror;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class App {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        URLBrowser urlBrowser = new URLBrowser();
        ResponseParser responseParser = new ResponseParser();
        ResponseCacheManager responseCacheManager = new ResponseCacheManager();

        responseCacheManager.loadCache();

        switch (args[0]) {
            case "-u":
                if (args.length < 2) {
                    System.out.println("Error: No URL provided");
                    System.out.println("Usage: go2web -u <URL>");
                    return;
                }
                String url = args[1];
                try {
                    String finalResponse = responseCacheManager.getFromCache(url);

                    if (finalResponse == null) {
                        String response = urlBrowser.makeRequest(url);
                        finalResponse = responseParser.parseResponse(response);

                        System.out.println("Caching response for: " + url);
                        responseCacheManager.saveToCache(url, finalResponse);
                    } else {
                        System.out.println("Using cached response for: " + url);
                    }

                    System.out.println(finalResponse);
                } catch (Exception e) {
                    System.out.println("Error making request: " + e.getMessage());
                }
                break;
            case "-s":
                if (args.length < 2) {
                    System.out.println("Error: No search prompt provided");
                    System.out.println("Usage: go2web -s <search-term>");
                    return;
                }

                StringBuilder searchTermBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    searchTermBuilder.append(args[i]).append(" ");
                }
                String searchTerm = searchTermBuilder.toString().trim();


                try {
                    String searchUrl = "https://duckduckgo.com/html/?q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());

                    String cachedResponse = responseCacheManager.getFromCache(searchUrl);

                    if (cachedResponse != null) {
                        System.out.println("Using cached search results for: " + searchTerm);
                        System.out.println(cachedResponse);
                        return;
                    } else {
                        String response = urlBrowser.makeRequest(searchUrl);
                        responseCacheManager.saveToCache(searchUrl, responseParser.displaySearchResults(response));
                        System.out.println("Caching response for: " + searchUrl);
                    }
                } catch (Exception e) {
                    System.out.println("Error performing search: " + e.getMessage());
                }
                break;
            default:
                printHelp();
                break;
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  go2web -u <URL>         # make an HTTP request to the specified URL and print the response");
        System.out.println("  go2web -s <search-term> # make an HTTP request to search the term and print top 10 results");
        System.out.println("  go2web -h               # show this help");
    }

}
