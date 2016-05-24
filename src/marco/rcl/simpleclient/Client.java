package marco.rcl.simpleclient;

import marco.rcl.shared.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Client {

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue();
    private static UserInteractor interactor = new UserInteractor(queue);

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("clientlogger","client");
        log.info("client started");
        Thread t = new Thread(interactor);
        t.start();
        while (true) {
            try {
                String s = queue.take();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
