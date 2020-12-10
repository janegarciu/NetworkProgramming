package udp;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Random;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {
public static Key key = null;
public static InetSocketAddress serverAddress = null;
public static DatagramChannel channel = null;
public static ByteBuffer buf = null;
    public static void main(String[] args) throws IOException {
        while (true) {
            try {

                int serverPort = 8210; // Server port
                serverAddress = new InetSocketAddress(InetAddress.getByName("localhost"), serverPort); // Server Address
                SocketAddress routerAddr = new InetSocketAddress("localhost", 8080); // router

                UDPClient udpc = new UDPClient();
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                String clientRequest;

                System.out.print("Please enter you client request: ");
                clientRequest = keyboard.readLine();

                if (clientRequest.contains("get") || clientRequest.contains("GET")) {
                    udpc.getRequest(routerAddr, serverAddress, clientRequest);
                } else if (clientRequest.contains("post") || clientRequest.contains("POST")) {
                    udpc.postRequest(routerAddr, serverAddress, clientRequest);
                } else {
                    channel = DatagramChannel.open();
                    Packet p = new Packet.Builder()
                            .setType(5)
                            .setPortNumber(serverAddress.getPort())
                            .setPeerAddress(serverAddress.getAddress())
                            .setPayload(clientRequest.getBytes())
                            .create();
                    channel.send(p.toBuffer(), routerAddr);
                    // Try to receive a packet within timeout.
                    channel.configureBlocking(false);
                    Selector selector = Selector.open();
                    channel.register(selector, OP_READ);
                    System.out.println("Waiting for the response... ");
                    selector.select(25000);

                    Set<SelectionKey> keys = selector.selectedKeys();

                    // If there is no response within timeout
                    if (keys.isEmpty()) {
                        System.out.println("No response after timeout");
                    } else {

                        // We just want a single response.
                        buf = ByteBuffer.allocate(Packet.MAX_LEN);
                        SocketAddress router = channel.receive(buf);
                        buf.flip();
                        Packet resp = Packet.fromBuffer(buf);
                        String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("Received message: " + payload);
                        key = generateDHKeys(payload, routerAddr);
                    }
                }
                if(clientRequest.equalsIgnoreCase("quit")){
                    break;
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private static Key generateKey(byte[] sharedKey) {
        // AES supports 128 bit keys. So, just take first 16 bits of DH generated key.
        byte[] byteKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            byteKey[i] = sharedKey[i];
        }

        // convert given key to AES format
        try {
            Key key = new SecretKeySpec(byteKey, "AES");

            return key;
        } catch (Exception e) {
            System.err.println("Error while generating key: " + e);
        }

        return null;
    }// End of post request

    public static Key generateDHKeys(String s, SocketAddress routerAddr) throws IOException {
        BigInteger p = null,g = null,A = null;
        String[] splitRequest = s.split(",");
        for (String number : splitRequest) {
            BigInteger bigInteger = new BigInteger(number.substring(number.indexOf("=") + 1).trim());
            if (number.contains("p")) {
                 p = bigInteger;
            } else if (number.contains("g")) {
                g = bigInteger;
            } else if (number.contains("A")) {
                A = bigInteger;
            }

        }
        //---implementation
        // receive prime number from server

        // generate secret b
        Random randomGenerator = new Random();
        BigInteger b = new BigInteger(1024, randomGenerator); // secret key b (private) (on client)

        // calculate public B
        BigInteger B = g.modPow(b, p); // calculated public client key (B=g^b(modp))

        // calculate secret key
        BigInteger decryptionKeyClient = A.modPow(b, p);

        System.out.println("Calculated key: " + decryptionKeyClient);

        // generate AES key
        key = generateKey(decryptionKeyClient.toByteArray());

        // send B to server
        System.out.println("Sending B value to server:" + B.toString());
        Packet packet = new Packet.Builder()
                .setType(6)
                .setPeerAddress(serverAddress.getAddress())
                .setPortNumber(serverAddress.getPort())
                .setPayload(B.toString().getBytes())
                .create();
        channel.send(packet.toBuffer(), routerAddr);
        return key;
    }

    public void getRequest(SocketAddress routerAddr, InetSocketAddress serverAddress, String clientRequest) throws IOException {
        // Variables
        String clientPayload;
        String method = "GET";
        String url;
        String version = "HTTP\\1.0";
        String header = null;
        String[] splitRequest;
        int sequenceNumber = 1;
        int length;

        // Preparing http client request
        splitRequest = clientRequest.split(" ");
        length = splitRequest.length;

        url = splitRequest[length - 1];

        for (int i = 0; i < length; i++) {
            if (splitRequest[i].contains("-h")) {
                header = splitRequest[i + 1];
            }
        }

        // http client payload
        clientPayload = method + " " + url + " " + version + "\\" + header + "\\\\";

        //------------------ENTIRE GET REQUEST OPERATION------------------------
        try (DatagramChannel channel = DatagramChannel.open()) {
            boolean booleanValue = false;
            sequenceNumber++;

            // Three way handshake
            do {
                String msg = "SYN";

                // Creating packet
                Packet p = new Packet.Builder()
                        .setType(1)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(msg.getBytes())
                        .create();
                channel.send(p.toBuffer(), routerAddr);
                System.out.println("CLIENT: Sending SYN request to the router address: " + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response... ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                // If there is no response within timeout
                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {

                    // We just want a single response.
                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

                    System.out.println("CLIENT: Received from server and connected to server!");
                    System.out.println();

                    if (resp.getType() == 2 && resp.getSequenceNumber() == sequenceNumber) {
                        booleanValue = true;
                    }
                    keys.clear();
                }

            } while (!booleanValue);
            //-------------------End of three way handshake-------------------


            // ------------------Sending Get Request--------------------------
            boolean booleanValue2 = false;

            do {
                sequenceNumber++;

                String message = AES.encryptFile(clientPayload, key);

                Packet packet = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(1L)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(message.getBytes())
                        .create();
                channel.send(packet.toBuffer(), routerAddr);

                System.out.println("CLIENT: Sending this message: " + clientPayload + " to the router address" + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response....");
                System.out.println(" ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {
                    // We just want a single response.
                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

                    System.out.println("Encrypted payload from server: " + payload);
                    System.out.println();
                    String decryptedPayload = AES.decryptFile(payload, key);
                    System.out.println("CLIENT: Payload: " + decryptedPayload);
                    System.out.println();

                    booleanValue2 = true;

                    keys.clear();
                }

            } while (!booleanValue2);


            // --------------Closing connection-------------------------

            boolean booleanValue3 = false;

            do {
                String msg = "FIN";
                sequenceNumber++;

                Packet p = new Packet.Builder()
                        .setType(3)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(msg.getBytes())
                        .create();
                channel.send(p.toBuffer(), routerAddr);
                System.out.println("CLIENT: Sending FIN request to the router address: " + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response... ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {

                    // We just want a single response.
                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

                    System.out.println("CLIENT: Received FIN-ACK from server and connected to server!");
                    System.out.println();

                    if (resp.getType() == 4 && resp.getSequenceNumber() == sequenceNumber) {
                        booleanValue3 = true;
                    }

                    keys.clear();
                }

            } while (!booleanValue3);
            //-------------END OF CLOSING CONNECTION-------------------

            System.out.println("END OF CLIENT");

        } // End Of Get Request
    }

    // POST REQUEST
    public void postRequest(SocketAddress routerAddr, InetSocketAddress serverAddress, String clientRequest) throws IOException {
        // Variables
        String clientPayload;
        String entityBody = null;
        String method = "POST";
        String url;
        String version = "HTTP\\1.0";
        String header = null;
        String[] splitRequest;
        int sequenceNumber = 1;
        int length;

        splitRequest = clientRequest.split(" ");
        length = splitRequest.length;

        url = splitRequest[length - 1];

        for (int i = 0; i < length; i++) {
            if (splitRequest[i].contains("-h")) {
                header = splitRequest[i + 1];
            }

            if (splitRequest[i].contains("-d")) {
                entityBody = splitRequest[i + 1];
            }
        }

        clientPayload = method + " " + url + " " + version + "\\" + header + "\\\\" + entityBody;

        // Sending to Server
        try (DatagramChannel channel = DatagramChannel.open()) {
            boolean booleanValue = false;
            sequenceNumber++;

            // Three way handshake
            do {
                String msg = "SYN";

                Packet p = new Packet.Builder()
                        .setType(1)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(msg.getBytes())
                        .create();
                channel.send(p.toBuffer(), routerAddr);
                System.out.println("CLIENT: Sending SYN request to the router address: " + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response... ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {

                    // We just want a single response.
                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

                    System.out.println("CLIENT: Received SYN-ACK from server and connected to server!");
                    System.out.println();

                    if (resp.getType() == 2 && resp.getSequenceNumber() == sequenceNumber) {
                        booleanValue = true;
                    }

                    keys.clear();
                }

            } while (!booleanValue);  // End of three way handshake


            // ------------------Sending Post Request----------------------------
            boolean booleanValue2 = false;
            sequenceNumber++;

            String message = AES.encryptFile(clientPayload, key);
            do {
                Packet packet = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(message.getBytes())
                        .create();
                channel.send(packet.toBuffer(), routerAddr);

                System.out.println("CLIENT: Sending this message: " + clientPayload + " to the router address" + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response....");
                System.out.println(" ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {
                    // We just want a single response.
                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Encrypted payload from server: " + payload);
                    System.out.println();
                    String decryptedPayload = AES.decryptFile(payload, key);
                    System.out.println("CLIENT: Payload: " + decryptedPayload);
                    System.out.println();

                    booleanValue2 = true;

                    keys.clear();
                }

            } while (!booleanValue2);
            // ------------------END of Sending Post Request-------------------


            // ---------------------Closing connection-------------------------
            boolean booleanValue3 = false;
            sequenceNumber++;

            do {
                String msg = "FIN";

                Packet packet = new Packet.Builder()
                        .setType(3)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(msg.getBytes())
                        .create();
                channel.send(packet.toBuffer(), routerAddr);
                System.out.println("CLIENT: Sending FIN request to the router address: " + routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                System.out.println("Waiting for the response... ");
                selector.select(15000);

                Set<SelectionKey> keys = selector.selectedKeys();

                if (keys.isEmpty()) {
                    System.out.println("No response after timeout");
                } else {

                    ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                    SocketAddress router = channel.receive(buf);
                    buf.flip();
                    Packet resp = Packet.fromBuffer(buf);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);

                    System.out.println("CLIENT: Received FIN-ACK from server and connected to server!");
                    System.out.println();

                    if (resp.getType() == 4 && resp.getSequenceNumber() == sequenceNumber) {
                        booleanValue3 = true;
                    }

                    keys.clear();
                }

            } while (!booleanValue3);
            // ---------------------END Of Closing connection-----------------------

            System.out.println("END OF CLIENT");

        }

    }

}