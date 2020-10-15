package TCPServer;

import Models.Person;
import ThreadsRunner.Initializer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiTCPServerThread extends Thread {
    private Socket socket = null;
    private DataInputStream in;
    boolean isGlobalShutdown = false;

    public MultiTCPServerThread(DataInputStream in, Socket socket) {
        super("MultiServerThread");
        this.socket = socket;
        this.in = in;
    }

    public void run() {
        while (true) {

            System.out.println("New client connected");

            // takes input from the client socket
            try {
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line = "";

            // reads message from client until "Over" is sent
            while (!line.contains("Over")) {
                if (line.contains("Global shutdown"))
                    isGlobalShutdown = true;
                try {
                    line = in.readUTF();
                    System.out.println(line);
                    getField(line);

                } catch (IOException i) {
                    break;
                }
            }

            System.out.println("Closing connection");

            // close connection
            try {
                socket.close();
                in.close();
                currentThread().interrupt();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void getField(String s) {
        try {
            for (Object x : Initializer.personList) {
                List<Field> fields = new ArrayList<>();
                for (int i = 0; i < Person.class.getFields().length; i++) {
                    fields.add(Person.class.getFields()[i]);
                }
                for (Field field : fields) {
                    if (s.contains(field.getName())) {
                        Client.out.writeUTF("--------  Column:" +s+"---------");
                        Field newField = Person.class.getField(field.getName());
                        Object fieldValue = newField.get(x);
                        System.out.println(fieldValue);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {

        }

    }
}
