package md.mirrerror.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseParser {

    private static final Pattern LINK_PATTERN = Pattern.compile("<a class=\"result__a\" href=\"([^\"]+)\"[^>]*>([^<]+)</a>");
    private static final Pattern UDDG_PATTERN = Pattern.compile("uddg=([^&]+)");
    private static final Pattern GENERIC_PATTERN = Pattern.compile("<a[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>");

    private final Map<Integer, String> lastSearchResults = new HashMap<>();

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

    public String displaySearchResults(String response) {
        int headerEnd = response.indexOf("\r\n\r\n");
        if (headerEnd == -1) {
            System.out.println("Invalid response format");
            return "";
        }

        lastSearchResults.clear();

        String body = response.substring(headerEnd + 4);

        Matcher matcher = LINK_PATTERN.matcher(body);

        int count = 0;
        Set<String> uniqueUrls = new HashSet<>();
        StringBuilder searchResultsBuilder = new StringBuilder();

        System.out.println("Search results:");

        while (matcher.find() && count < 10) {
            String url = matcher.group(1);
            String title = matcher.group(2).trim();

            if (url.startsWith("/l/?")) {
                Matcher uddgMatcher = UDDG_PATTERN.matcher(url);
                if (uddgMatcher.find()) {
                    try {
                        url = URLDecoder.decode(uddgMatcher.group(1), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException ignored) {}
                }
            }

            if (addSearchResult(searchResultsBuilder, count, url, title, uniqueUrls)) count++;
        }

        if (count == 0) {
            Matcher genericMatcher = GENERIC_PATTERN.matcher(body);

            while (genericMatcher.find() && count < 10) {
                String url = genericMatcher.group(1);
                String title = genericMatcher.group(2).trim();

                if (url.startsWith("#") || url.startsWith("javascript:") ||
                        url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif") ||
                        title.length() < 5) {
                    continue;
                }

                if (addSearchResult(searchResultsBuilder, count, url, title, uniqueUrls)) count++;
            }
        }

        if (count == 0) System.out.println("No search results have been found.");

        return searchResultsBuilder.toString();
    }

    private String formatSearchUrl(String url) {
        if (!url.startsWith("http")) return "https://duckduckgo.com" + url;
        return url;
    }

    private boolean addSearchResult(StringBuilder searchResultsBuilder, int count, String url, String title, Set<String> uniqueUrls) {
        title = decodeHtmlEntities(title);

        url = formatSearchUrl(url);

        if (uniqueUrls.add(url)) {
            lastSearchResults.put(count + 1, url);
            searchResultsBuilder.append(String.format("%d. %s\n   %s\n\n", count + 1, title, url));
            return true;
        }

        return false;
    }

    public String getSearchResult(int index) {
        String url = lastSearchResults.get(index);

        if (url == null) {
            System.out.println("Invalid search result index");
            return null;
        }

        return url;
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
