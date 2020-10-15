package TCPServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;


public class Server {
    public MultiTCPServerThread thread;
    public static ServerSocket serverSocket;

    public Server(int port) {
        DataInputStream in = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");


            while (listening) {
                thread = new MultiTCPServerThread(in, serverSocket.accept());
                thread.start();
                thread.interrupt();
                if(thread.isGlobalShutdown==true) {
                    serverSocket.close();
                    break;
                }
                System.exit(0);
            }
        } catch (IOException i) {
            System.out.println(i);
        }

    }

}

