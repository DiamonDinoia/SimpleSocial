package marco.rcl.simpleclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by marko on 23/05/2016.
 */
public class UserInteractor implements Runnable {

    private LinkedBlockingQueue<String> queue = null;

    UserInteractor(LinkedBlockingQueue<String> q){
        queue = q;
    }

    public void run(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            try {
                queue.put(reader.readLine());
            } catch (InterruptedException | IOException e) {
                System.err.println("Thread UserInteractor Failed");
                e.printStackTrace();
            }
        }
    }

}
