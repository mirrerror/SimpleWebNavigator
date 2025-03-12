package md.mirrerror;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class App {
    public static void main(String[] args) {
        if (args.length == 0 || "-h".equals(args[0])) {
            printHelp();
        } else if ("-u".equals(args[0]) && args.length > 1) {
            fetchUrl(args[1]);
        } else if ("-s".equals(args[0]) && args.length > 1) {
            search(args[1]);
        } else {
            System.out.println("Invalid command. Use -h for help.");
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  go2web -u <URL>         # Fetch and print response from URL");
        System.out.println("  go2web -s <search-term> # Search and print top 10 results");
        System.out.println("  go2web -h               # Show this help");
    }

    private static void fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            boolean isHttps = url.getProtocol().equalsIgnoreCase("https");
            String host = url.getHost();
            int port = isHttps ? 443 : 80;
            String path = url.getPath().isEmpty() ? "/" : url.getPath();
            if (url.getQuery() != null && !url.getQuery().isEmpty()) {
                path += "?" + url.getQuery();
            }

            Socket socket;
            if (isHttps) {
                socket = SSLSocketFactory.getDefault().createSocket(host, port);
            } else {
                socket = new Socket(host, port);
            }

            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                writer.println("GET " + path + " HTTP/1.1");
                writer.println("Host: " + host);
                writer.println("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                writer.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                writer.println("Accept-Language: en-US,en;q=0.5");
                writer.println("Connection: close");
                writer.println("");

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                String responseStr = response.toString();
                if (responseStr.contains("HTTP/1.1 301") || responseStr.contains("HTTP/1.1 302") ||
                        responseStr.contains("HTTP/1.1 303") || responseStr.contains("HTTP/1.1 307") ||
                        responseStr.contains("HTTP/1.1 308")) {
                    String newLocation = extractRedirectLocation(responseStr);
                    if (newLocation != null) {
                        System.out.println("Redirecting to: " + newLocation);
                        fetchUrl(newLocation);
                        return;
                    }
                }

                int bodyStart = responseStr.indexOf("\r\n\r\n");
                if (bodyStart == -1) {
                    bodyStart = responseStr.indexOf("\n\n");
                }

                if (bodyStart != -1) {
                    String body = responseStr.substring(bodyStart);
                    System.out.println(cleanHtml(body));
                } else {
                    System.out.println(cleanHtml(responseStr));
                }
            }
            socket.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String extractRedirectLocation(String response) {
        Pattern pattern = Pattern.compile("Location: (.*?)\\r?\\n");
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static void search(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String searchUrl = "https://duckduckgo.com/lite/?q=" + encodedQuery;
            System.out.println("Searching for: " + query);
            fetchUrl(searchUrl);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error encoding search query: " + e.getMessage());
        }
    }

    private static String cleanHtml(String input) {
        String noTags = input.replaceAll("<[^>]+>", " ");

        String decodedHtml = noTags.replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&#39;", "'");

        return decodedHtml.replaceAll("\\s+", " ").trim();
    }
}