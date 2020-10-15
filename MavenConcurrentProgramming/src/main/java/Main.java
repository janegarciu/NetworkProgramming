import Deserializer.JsonDeserializer;
import TCPServer.Server;
import ThreadsRunner.Initializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {

    private static class Program {
        private synchronized void waitMethod() {
            try {
                this.wait(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        JsonDeserializer jsonDeserializer = new JsonDeserializer();
        Map<String, String> map = jsonDeserializer.parseHomeResponse().getLink();
        LinkedList<Map.Entry<String, String>> queue = new LinkedList<>(map.entrySet());
        Initializer.executeThread(queue);
        if (Initializer.threadPool.getActiveCount() != 0) {
            Program object = new Program();
            object.waitMethod();
            if (Initializer.threadPool.getActiveCount() == 0) {
                Initializer.threadPool.shutdown();
                Initializer.threadPool.awaitTermination(20, TimeUnit.SECONDS);
            }
        }
        DataParser.convertData(); //****Parse all data obtained from roots
        Server server = new Server(5500);
    }
}

