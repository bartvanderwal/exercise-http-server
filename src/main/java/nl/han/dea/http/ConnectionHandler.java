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
    private static final String HTTP_STATUS_404 = "404 NOT FOUND";
    private static final String HTTP_STATUS_501 = "501 NOT IMPLEMENTED";
    private static final String HTTP_STATUS_505 = "505 HTTP VERSION NOT SUPPORTED";
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

            parseHttpRequest(inputStreamReader, outputStreamWriter);
            writeResponseMessage(outputStreamWriter);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseHttpRequest(BufferedReader inputStreamReader, BufferedWriter outputStreamWriter) throws IOException {
        boolean startLine = true;
        String requestLine;
        while ((requestLine = inputStreamReader.readLine()) != null) {
            System.out.println(requestLine);
            if (startLine) {
                startLine = false;
                processStartLine(outputStreamWriter, requestLine);
            } else {
                if (lineMarksEndOfRequest(requestLine)) {
                    return;
                }
            }
        }
    }

    private void processStartLine(BufferedWriter outputStreamWriter, String requestLine) throws IOException {
        String startLineTokens[] = requestLine.split(" ");
        if (!"GET".equals(startLineTokens[0])) {
            outputStreamWriter.write(generateHeader(HTTP_STATUS_501, null));
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
        } else if (!"/index.html".equals(startLineTokens[1])) {
            outputStreamWriter.write(generateHeader(HTTP_STATUS_404, null));
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
        } else if (!"HTTP/1.1".equals(startLineTokens[2])) {
            outputStreamWriter.write(generateHeader(HTTP_STATUS_505, null));
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
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
