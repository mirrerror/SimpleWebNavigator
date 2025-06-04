# SimpleWebNavigator

A command-line tool implemented in Java and Batch that performs HTTP requests and search queries without utilizing built-in or third-party HTTP libraries.

## Demo

![Demo](demo/demo.gif)

## Features

* **HTTP Request**: Fetches and displays the content of a specified URL.
* **Search Query**: Performs a search using a specified term and displays the top 10 results.
* **Help Menu**: Provides guidance on how to use the tool.

## Usage

```
go2web -u <URL>         # Make an HTTP request to the specified URL and print the response
go2web -s <search-term> # Search the term using your favorite search engine and print top 10 results
go2web -h               # Show this help
```

## Features

* **No Built-in/Third-party HTTP Libraries**: Implemented HTTP requests using raw sockets.
* **Command-line Interface**: The application is strictly CLI-based, launched via the `go2web` executable.
* **Search Result Accessibility**: Each search result includes a direct link, allowing users to access the pages directly from the CLI.
* **HTTP Redirect Handling**: Implemented logic to follow HTTP redirects, ensuring the final content is retrieved.
* **HTTP Caching Mechanism**: Introduced a simple in-memory caching system to store and retrieve responses and a JSON persistent storage for it, reducing redundant network calls.

## Getting Started

1. **Clone the Repository**:

   ```
   git clone https://github.com/mirrerror/SimpleWebNavigator.git
   cd SimpleWebNavigator
   ```

2. **Build the Project**:

   Ensure you have Maven installed. Then run:

   ```
   mvn clean package
   ```

3. **Run the Application**:

   Use the provided `go2web.bat` script to execute the application:

   ```
   go2web -h
   ```
