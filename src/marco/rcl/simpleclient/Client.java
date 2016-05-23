package marco.rcl.simpleclient;

import java.util.concurrent.LinkedBlockingQueue;

public class Client {

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue();
    private static UserInteractor interactor = new UserInteractor(queue);

    public static void main(String[] args) {
        System.out.println("Hello Marco!");
        Thread t = new Thread(interactor);
        t.start();
        while (true){
            try {
                String s = queue.take();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
