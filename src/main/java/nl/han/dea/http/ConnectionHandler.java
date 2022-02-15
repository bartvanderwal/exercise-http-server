package nl.han.dea.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class ConnectionHandler {

    private static final String HTTP_HEADERS = "HTTP/1.1 %d %s\n" +
            "Date: %s\n" +   // "Date: Mon, 27 Aug 2018 14:08:55 +0200\n" +
            "HttpServer: Simple DEA Webserver\n" +
            "Content-Length: %d\n" +
            "Content-Type: text/html\n";

    private static final String HTTP_BODY = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "<title>Simple Http Server</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>Hi DEA folks!</h1>\n" +
            "<p>This is a simple line in html.</p>\n" +
            "</body>\n" +
            "</html>\n" +
            "\n";
    private Socket socket;

    // Het ingelezen pad uit http request.
    String path;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        handle();
    }

    HtmlPageReader htmlPageReader = new HtmlPageReader();

    public void handle() {
        try {
            var inputStreamReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            var outputStreamWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            parseRequest(inputStreamReader);
            writeResponse(outputStreamWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRequest(BufferedReader inputStreamReader) throws IOException {
        var request = inputStreamReader.readLine();

        if (request==null) {
            return;
        }
        path = request.split(" ")[1];
        if ("/".equals(path)) {
            path = "index.html";
        }
        // TODO: Plaatjes zoals favicon doorsturen. Voorlopig nog even de favicon.ico inslikken.
        if (path == "/favicon.ico") {
            return;
        }
        while (request != null && !request.isEmpty()) {
            System.out.println(request);
            request = inputStreamReader.readLine();
        }
    }

    private void writeResponse(BufferedWriter outputStreamWriter) {
        try {
            FileInfo fileInfo = null;
            var returnStatus = new ReturnStatus();
            try {
                fileInfo = htmlPageReader.readFile(path);
                returnStatus.code = 200;
                returnStatus.message = "OK";
            } catch (A404Exception e) {
                fileInfo = htmlPageReader.readFile("404.html");
                returnStatus.code = 404;
                returnStatus.message = "Page Not found";
            }
            Calendar calendar = Calendar.getInstance();
            // Voorbeeld uit request: date: Fri, 11 Feb 2022 15:40:01 GMT

            LocalDateTime localDateTime = LocalDateTime.now();
            var httpDateFormatter = DateTimeFormatter
                    .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
            String date = localDateTime.format(httpDateFormatter);
            // LocalDate parsedDate = LocalDate.parse(text, formatter);

            var httpHeaders = String.format(HTTP_HEADERS, returnStatus.code, returnStatus.message, date, fileInfo.getContentLength());
            outputStreamWriter.write(httpHeaders);
            outputStreamWriter.newLine();
            outputStreamWriter.write(fileInfo.getContent());
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
