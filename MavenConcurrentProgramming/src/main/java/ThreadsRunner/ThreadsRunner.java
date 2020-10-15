package ThreadsRunner;

import HttpRequestor.HttpRequestor;
import Models.RoutesResponse;

import java.util.LinkedList;
import java.util.Map;


public class ThreadsRunner implements Runnable {
    private final String route;
    private final String token;
    private final LinkedList<Map.Entry<String, String>> queue = new LinkedList<>();
    HttpRequestor httpRequestor = new HttpRequestor();

    public ThreadsRunner(String route, String token) {
        this.route = route;
        this.token = token;
    }

    @Override
    synchronized public void run() {
        System.out.println("------------Response of------------" + "\n" + Thread.currentThread().getName());
        System.out.println("$$$$$$$$$$$Current route$$$$$$$$$$$" + "\n" + route );
        RoutesResponse routesResponse = httpRequestor.getRequest(route, token).getBody().as(RoutesResponse.class);
        if (routesResponse.getLink() != null) {
            queue.addAll(routesResponse.getLink().entrySet());
            Initializer.executeThread(queue);
            if (routesResponse.getMime_type() == null)
                Initializer.mapData.put(routesResponse.getData(), "application/json");
            else
                Initializer.mapData.put(routesResponse.getData(), routesResponse.getMime_type());

        } else {
            if (routesResponse.getMime_type() == null)
                Initializer.mapData.put(routesResponse.getData(), "application/json");
            else
                Initializer.mapData.put(routesResponse.getData(), routesResponse.getMime_type());
        }
    }
}
