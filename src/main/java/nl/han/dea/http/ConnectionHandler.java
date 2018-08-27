package nl.han.dea.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectionHandler implements Runnable {

    private static final String SERVER_NAME = "Simple DEA Webserver";
    private static final String HTTP_STATUS_200 = "200 OK";
    private static final String INDEX_HTML_PAGE = "pages/index.html";

    private final String HEADER_TEMPLATE = "HTTP/1.1 {{HTTP_STATUS}}\n" +
            "Date: {{DATE}}\n" +
            "HttpServer: " + SERVER_NAME + "\n" +
            "Content-Length: {{CONTENT_LENGTH}}\n" +
            "Content-Type: text/html\n";


    private Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader inputStreamReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            BufferedWriter outputStreamWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
            String requestLine;
            while ((requestLine = inputStreamReader.readLine()) != null) {
                System.out.println(requestLine);
                if (lineMarksEndOfRequest(requestLine)) {
                    writeResponseMessage(outputStreamWriter);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeResponseMessage(BufferedWriter outputStreamWriter) {

        try {
            outputStreamWriter.write(generateHeader(HTTP_STATUS_200, INDEX_HTML_PAGE));
            outputStreamWriter.newLine();
            outputStreamWriter.write(readFile(INDEX_HTML_PAGE));
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(getPath(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateHeader(String status, String filename) {
        String header = HEADER_TEMPLATE
                .replace("{{DATE}}",
                        OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME))
                .replace("{{HTTP_STATUS}}", status);

        String contentLength = Long.toString(0);
        if (filename != null) {
            contentLength = Long.toString(getPath(filename).toFile().length());
        }
        header = header.replace("{{CONTENT_LENGTH}}", contentLength);
        return header;
    }

    private Path getPath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).toPath();
    }

    private boolean lineMarksEndOfRequest(String line) {
        return line.isEmpty();
    }

}
