package md.mirrerror;

import md.mirrerror.web.URLBrowser;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        URLBrowser urlBrowser = new URLBrowser();

        switch (args[0]) {
            case "-u":
                if (args.length < 2) {
                    System.out.println("Error: No URL provided");
                    System.out.println("Usage: go2web -u <URL>");
                    return;
                }

                String url = args[1];

                try {
                    System.out.println(urlBrowser.makeRequest(url, true));
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
                    System.out.println(urlBrowser.makeSearch(searchTerm));

                    System.out.print("Type the number of the search result you want to view, or press Enter to cancel: ");

                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        System.out.println("Search canceled.");
                    } else {
                        try {
                            int index = Integer.parseInt(input);
                            if (index > 0 && index <= 10) {
                                String resultUrl = urlBrowser.getResponseParser().getSearchResult(index);
                                if (resultUrl != null) {
                                    System.out.println(urlBrowser.makeRequest(resultUrl, true));
                                    Desktop.getDesktop().browse(new URI(resultUrl));
                                }
                            } else {
                                System.out.println("Invalid search result index");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                        }
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
