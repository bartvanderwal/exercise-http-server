package nl.han.dea.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private int tcpPort;

    public HttpServer(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    ServerSocket serverSocket;

    public static void main(String[] args) {
        new HttpServer(8383).startServer();
    }

    public void startServer() {

        System.out.println("Server accepting requests on port " + tcpPort);
        var aantalThreads = 0;

        try {
            serverSocket = new ServerSocket(this.tcpPort);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error bij aanmaken socket op poort " + tcpPort + ". Poort is waarschijnlijk al in gebruikt on port", e);
        }

        while(true) {
            try {
                Socket acceptedSocket = serverSocket.accept();
                var connectionHandler = new ConnectionHandler(acceptedSocket);
                new Thread(() -> connectionHandler.handle()).start();
                aantalThreads++;
                System.out.println("Aantal request handling threads: " + aantalThreads);
            } catch (IOException e) {
                throw new RuntimeException("Error in handling request on port " + tcpPort, e);
            }
        }
    }
}
