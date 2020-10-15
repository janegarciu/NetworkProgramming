package ThreadsRunner;

import Deserializer.JsonDeserializer;
import Models.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Initializer {
    public static JsonDeserializer jsonDeserializer = new JsonDeserializer();
    public static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
    ;
    public static String token = jsonDeserializer.parseRegisterResponse().getAccess_token();
    public static Map<String, String> mapData = new HashMap<>();
    public static ArrayList<Person> personList = new ArrayList<>();

    public static void executeThread(LinkedList<Map.Entry<String, String>> queue) {
        for (int i = 0; i < queue.size(); i++) {
            ThreadsRunner threadsRunner = new ThreadsRunner(queue.get(i).getValue(), token);
            threadPool.execute(threadsRunner);
        }
    }
}
