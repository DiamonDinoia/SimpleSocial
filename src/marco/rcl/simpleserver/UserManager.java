package marco.rcl.simpleserver;

import java.io.*;
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
    boolean managing = false;
    public UserManager(Logger log) {
        this.log = log;
        connections = new LinkedBlockingQueue<>();
    }

    public void add(Socket socket){
        connections.add(socket);
    }

    private void manager(Socket socket){
        try {
            ObjectInputStream in =  new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startManagingUsers(){
        managing=true;
        log.info("Start managing Users");
        while (managing){
            try {
                manager(connections.take());
            } catch (InterruptedException e) {
                log.severe("Failed managing Users " + e.toString());
                e.printStackTrace();
                managing=false;
                break;
            }

        }
    }
}
