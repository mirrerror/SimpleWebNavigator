package md.mirrerror;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseParser {

    private final Map<Integer, String> searchResults = new HashMap<>();

    public String parseResponse(String response) {
        int headerEnd = response.indexOf("\r\n\r\n");
        if (headerEnd == -1) return "Invalid response format";

        String body = response.substring(headerEnd + 4)
                .replaceAll("<!DOCTYPE[^>]*>", "")
                .replaceAll("<!--.*?-->", "")
                .replaceAll("(?s)<script.*?</script>", "")
                .replaceAll("(?s)<style.*?</style>", "")
                .replaceAll("style=\"[^\"]*\"", "")
                .replaceAll("class=\"[^\"]*\"", "")
                .replaceAll("id=\"[^\"]*\"", "")
                .replaceAll("(?s)<head>.*?</head>", "")
                .replaceAll("(?s)<nav.*?</nav>", "")
                .replaceAll("(?s)<header.*?</header>", "")
                .replaceAll("(?s)<footer.*?</footer>", "")
                .replaceAll("(?s)<form.*?</form>", "")
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();

        body = decodeHtmlEntities(body);

        String[] lines = body.split("\\. ");
        StringBuilder meaningfulContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.length() > 3 && !line.matches("[\\s\\d\\W]+")) {
                meaningfulContent.append(line).append(". ");
            }
        }

        return meaningfulContent.toString().trim();
    }

    public void displaySearchResults(String response) {
        searchResults.clear();

        int headerEnd = response.indexOf("\r\n\r\n");
        if (headerEnd == -1) {
            System.out.println("Invalid response format");
            return;
        }

        String body = response.substring(headerEnd + 4);

        Pattern linkPattern = Pattern.compile("<a class=\"result__a\" href=\"([^\"]+)\"[^>]*>([^<]+)</a>");
        Matcher matcher = linkPattern.matcher(body);

        int count = 0;
        Set<String> uniqueUrls = new HashSet<>();

        System.out.println("Search results:");

        while (matcher.find() && count < 10) {
            String url = matcher.group(1);
            String title = matcher.group(2).trim();

            if (url.startsWith("/l/?")) {
                Pattern uddgPattern = Pattern.compile("uddg=([^&]+)");
                Matcher uddgMatcher = uddgPattern.matcher(url);
                if (uddgMatcher.find()) {
                    try {
                        url = URLDecoder.decode(uddgMatcher.group(1), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException ignored) {}
                }
            }

            if (addSearchResult(count, url, title, uniqueUrls)) count++;
        }

        if (count == 0) {
            Pattern genericPattern = Pattern.compile("<a[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>");
            Matcher genericMatcher = genericPattern.matcher(body);

            while (genericMatcher.find() && count < 10) {
                String url = genericMatcher.group(1);
                String title = genericMatcher.group(2).trim();

                if (url.startsWith("#") || url.startsWith("javascript:") ||
                        url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif") ||
                        title.length() < 5) {
                    continue;
                }

                if (addSearchResult(count, url, title, uniqueUrls)) count++;
            }
        }

        if (count == 0) System.out.println("No search results have been found.");
    }

    private String formatUrl(String url) {
        if (!url.startsWith("http")) return "https://duckduckgo.com" + url;
        return url;
    }

    private boolean addSearchResult(int count, String url, String title, Set<String> uniqueUrls) {
        title = decodeHtmlEntities(title);

        url = formatUrl(url);

        if (uniqueUrls.add(url)) {
            System.out.printf("%d. %s\n   %s\n\n", count + 1, title, url);
            searchResults.put(count, url);
            return true;
        }

        return false;
    }

    private String decodeHtmlEntities(String input) {
        if (input == null) return null;

        input = input.replace("&quot;", "\"")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replace("&#x27;", "'")
                .replace("&#39;", "'")
                .replace("&#x22;", "\"")
                .replace("&#60;", "<")
                .replace("&#62;", ">");

        return input;
    }


}
