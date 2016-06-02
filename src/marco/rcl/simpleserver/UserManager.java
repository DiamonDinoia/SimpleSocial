package marco.rcl.simpleserver;


import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This class is used to manage users
 * Created by Marco on 29/05/16.
 */
class UserManager {
    private static Logger log = Server.getLog();
    private ConcurrentHashMap<String,User> users;
    private final static String userFilename = "./ServerData/Users.ser";
    private LinkedBlockingQueue<Socket> connections;

    UserManager() {
        users = DiskManager.restoreFromDisk(userFilename);
        if (users == null) throw new RuntimeException("problems restoring users");
        connections = new LinkedBlockingQueue<>();
    }


}
