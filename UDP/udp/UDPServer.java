package udp;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UDPServer extends Thread {
    public static BigInteger a = null;
    public static BigInteger p = null;
    // HTTP request
    static String method;
    static String url;
    static String requestVersion;
    static String requestHeader;
    static String requestEntityBody;
    // HTTP response
    static String responseVersion = "HTTP\\1.0";
    static String statusCode;
    static String phrase;
    static String responseHeader;
    static String responseEntityBody;
    static String serverResponse;
    private static Key key;
    boolean listening = true;

    // Get File method
    public static void getFileResponse(String directory) {

        File fileDirectory = new File(directory);
        File[] listFiles = fileDirectory.listFiles();

        for (File file : listFiles) {
            if (file.isFile()) {
                responseEntityBody += "\n" + file.getName() + "\n";
            }
        }

        statusCode = "200";
        phrase = "OK";
        responseHeader = requestHeader;
    }

    // ---------------------------SUPPORTING METHODS-----------------------------------

    // Get method
    public static void getResponse(String directory, String url) {

        System.out.println("Checking if file exists...");
        String filePath = directory + "/" + url;
        File file = new File(filePath);

        // Check if file exists in the directory
        if (file.exists()) {
            try {
                System.out.println("Trying to read from files...");
                readFile(filePath);

            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            statusCode = "404";
            phrase = "Not Found";
        }
    }

    // Post method
    public static void postResponse(String directory, String url, String requestEntityBody) {
        File file = new File(directory + url);

        if (file.exists()) {

            PrintWriter pw = null;

            try {
                pw = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pw.print(requestEntityBody);
            pw.close();

            statusCode = "200";
            phrase = "OK";
            responseHeader = requestHeader + "\\" + "Content-length: " + requestEntityBody.length() + "\\" + "Content-Type: text\\html";
            responseEntityBody = requestEntityBody;
        } else {

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter pw = null;

            try {
                pw = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pw.print(requestEntityBody);
            pw.close();

            statusCode = "201";
            phrase = "Created";
            responseHeader = requestHeader + "\\" + "Content-length: " + requestEntityBody.length() + "\\" + "Content-Type: text\\html";
            responseEntityBody = requestEntityBody;
        }

    }

    // Reads the selected file
    public static void readFile(String filePath) {
        System.out.println("Reading from the file...");

        Scanner keyboard = null;
        String text = "";

        try {
            keyboard = new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (keyboard.hasNext()) {
            text += keyboard.nextLine() + "\n";
        }

        statusCode = "200";
        phrase = "OK";
        responseEntityBody = text;
        responseHeader = requestHeader + "\\" + "Content-Length:" + responseEntityBody.length() + "\\" + "Content-Type: text\\html";
    }


    public static void reset() {
        // HTTP request
        method = null;
        url = null;
        requestVersion = null;
        requestHeader = null;
        requestEntityBody = null;

        // HTTP response
        responseVersion = "HTTP\\1.0";
        statusCode = null;
        phrase = null;
        responseHeader = null;
        responseEntityBody = null;
        serverResponse = null;
    }

    // generates usable SecretKey from given value. In default, user cannot create keys.
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
    }

    public void listenAndServe(int port, String directory) throws IOException, NoSuchAlgorithmException, InvalidParameterSpecException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));

            System.out.println("" + channel.getLocalAddress());

            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            while (true) {
                buf.clear();
                SocketAddress router = channel.receive(buf);

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                // DATA REQUEST
                if (packet.getType() == 0) {

                    reset();

                    String[] splitClientPayload;
                    String[] splitClientPayload2;

                    String payload = new String(packet.getPayload(), UTF_8);
                    String decryptedPayload = AES.decryptFile(payload, key);
                    System.out.println("Encrypted payload from client: " + payload);
                    System.out.println(" ");
                    System.out.println("Decrypted payload from client: " + decryptedPayload);
                    splitClientPayload = decryptedPayload.split(" ");
                    splitClientPayload2 = splitClientPayload[2].split("\\\\");

                    method = splitClientPayload[0];
                    url = splitClientPayload[1];
                    requestHeader = splitClientPayload2[1];

                    if (method.equals("get") || method.equals("GET")) {
                        if (url.equals("/") & url.length() == 1) {
                            getFileResponse(directory);
                        } else {
                            getResponse(directory, url);
                        }
                    } else {
                        if (splitClientPayload2.length > 2) {
                            requestEntityBody = splitClientPayload2[3];
                        }
                        postResponse(directory, url, requestEntityBody);
                    }

                    serverResponse = responseVersion + " " + statusCode + " " + phrase + "\\" + responseHeader + "\\" + "\\" + responseEntityBody;

                    System.out.println("SERVER: Sending this message to client: " + serverResponse);
                    String encryptedServerResp = AES.encryptFile(serverResponse, key);
                    Packet resp = packet.toBuilder()
                            .setPayload(encryptedServerResp.getBytes())
                            .create();
                    channel.send(resp.toBuffer(), router);
                }

                // CONNECTION REQUEST
                if (packet.getType() == 1) {
                    System.out.println("SERVER: Sending back syn ack");
                    Packet resp = packet.toBuilder()
                            .setType(2)
                            .create();
                    channel.send(resp.toBuffer(), router);
                }

                // CLOSING REQUEST
                if (packet.getType() == 3) {
                    System.out.println("SERVER: Sending back FYN-ACK");
                    Packet resp = packet.toBuilder()
                            .setType(4)
                            .create();
                    channel.send(resp.toBuffer(), router);
                }
                if (packet.getType() == 5) {
                    String payload = new String(packet.getPayload(), UTF_8);
                    System.out.println("Request from client:" + payload);

                    AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
                    paramGen.init(1024, new SecureRandom());
                    AlgorithmParameters params = paramGen.generateParameters();
                    DHParameterSpec dhSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);

                    Random randomGenerator = new Random();

                    a = new BigInteger(1024, randomGenerator); // secret key a (private) (on server)
                    p = dhSpec.getP(); // prime number (public) (generated on server)
                    BigInteger g = dhSpec.getG(); // primer number generator (public) (generated on server)
                    BigInteger A = g.modPow(a, p);
                    String serverKeys = "p=" + p.toString() + "," + "g=" + g.toString() + "," + "A=" + A.toString();
                    Packet resp = packet.toBuilder()
                            .setPayload(serverKeys.getBytes())
                            .create();
                    channel.send(resp.toBuffer(), router);

                }
                if (packet.getType() == 6) {
                    // receive calculated B
                    Packet clientKeyResponse = Packet.fromBuffer(buf);
                    String payload = new String(clientKeyResponse.getPayload(), StandardCharsets.UTF_8);
                    BigInteger B = null;
                    BigInteger bigInteger = new BigInteger(payload.substring(payload.indexOf("=") + 1).trim());
                    B = bigInteger;

                    // calculate secret key
                    BigInteger encryptionKeyServer = B.modPow(a, p);

                    System.out.println("Calculated key: " + encryptionKeyServer);

                    // generate AES key
                    key = generateKey(encryptionKeyServer.toByteArray());

                }
            }
        }
    }
}