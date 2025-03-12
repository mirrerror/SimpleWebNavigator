package md.mirrerror;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class URLBrowser {

    private static final int MAX_REDIRECTS = 10;

    private final Set<String> visitedUrls = new HashSet<>();
    private int redirectCount = 0;

    public String makeRequest(String urlString) throws Exception {
        visitedUrls.clear();
        redirectCount = 0;

        return fetchUrl(urlString);
    }

    private String fetchUrl(String urlString) throws Exception {
        if (visitedUrls.contains(urlString)) {
            throw new Exception("Redirect loop");
        }

        visitedUrls.add(urlString);

        if (redirectCount >= MAX_REDIRECTS) {
            throw new Exception("Too many redirects (limit: " + MAX_REDIRECTS + ")");
        }

        URL url = new URL(urlString);

        boolean isHttps = url.getProtocol().equalsIgnoreCase("https");
        int port = url.getPort() != -1 ? url.getPort() : (isHttps ? 443 : 80);

        Socket socket;
        if (isHttps) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket(url.getHost(), port);
        } else {
            socket = new Socket(url.getHost(), port);
        }

        String request = buildRequest(url);

        OutputStream out = socket.getOutputStream();
        out.write(request.getBytes());
        out.flush();

        InputStream in = socket.getInputStream();
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            response.write(buffer, 0, bytesRead);
        }

        socket.close();

        String responseStr = response.toString(StandardCharsets.UTF_8.name());

        String[] lines = responseStr.split("\r\n");
        if (lines.length > 0) {
            String statusLine = lines[0];
            if (statusLine.contains("301") || statusLine.contains("302") || statusLine.contains("307") || statusLine.contains("308")) {
                String location = null;
                for (String line : lines) {
                    if (line.toLowerCase().startsWith("location:")) {
                        location = line.substring("location:".length()).trim();
                        break;
                    }
                }

                if (location != null) {
                    if (location.startsWith("/")) {
                        location = url.getProtocol() + "://" + url.getHost() + location;
                    } else if (!location.startsWith("http")) {
                        String baseUrl = extractBaseUrl(url);
                        location = baseUrl + location;
                    }

                    redirectCount++;
                    System.out.println("Redirect #" + redirectCount + " to: " + location);
                    return fetchUrl(location);
                }
            }
        }

        return responseStr;
    }

    private static String buildRequest(URL url) {
        String path = url.getPath().isEmpty() ? "/" : url.getPath();
        if (url.getQuery() != null) {
            path += "?" + url.getQuery();
        }

        return "GET " + path + " HTTP/1.1\r\n" +
                "Host: " + url.getHost() + "\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml\r\n" +
                "Accept-Language: en-US,en;q=0.9\r\n" +
                "Connection: close\r\n\r\n";
    }

    private static String extractBaseUrl(URL url) {
        String baseUrl = url.getProtocol() + "://" + url.getHost();
        if (!url.getPath().isEmpty()) {
            String directory = url.getPath();
            if (!directory.endsWith("/")) {
                directory = directory.substring(0, directory.lastIndexOf('/') + 1);
            }
            baseUrl += directory;
        } else {
            baseUrl += "/";
        }
        return baseUrl;
    }

}
