package nl.han.dea.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConnectionHandler implements Runnable {

    private static final String HTTP_HEADER = "HTTP/1.1 200 OK\n" +
            "Date: Mon, 27 Aug 2018 14:08:55 +0200\n" +
            "HttpServer: Simple DEA Webserver\n" +
            "Content-Length: 90\n" +
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
            outputStreamWriter.write(HTTP_HEADER);
            outputStreamWriter.newLine();
            outputStreamWriter.write(readFile("pages/index.html"));
            outputStreamWriter.newLine();
            outputStreamWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean lineMarksEndOfRequest(String line) {
        return line.isEmpty();
    }

    private String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(getPath(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getPath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).toPath();
    }

}
