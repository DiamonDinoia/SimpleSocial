package marco.rcl.simpleserver;

import marco.rcl.shared.Command;
import marco.rcl.shared.Response;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This class is used to manage users
 * Created by Marco on 29/05/16.
 */
public class UserManager {
    private Logger log;
    private LinkedBlockingQueue<Socket> connections;


    public static Response commandResponce(Command command){

        return new Response();
    }
}
