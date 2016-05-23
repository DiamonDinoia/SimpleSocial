package marco.rcl.simpleserver;

import marco.rcl.shared.ConnectionParam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to manager the TCP connection with the clients
 */
public class ConnectionManager {
    private ServerSocket serverSocket;
    private int port = ConnectionParam.PORT;
    private InetAddress address = ConnectionParam.ADDRESS;
    private int backlog = 100;
    private final Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Contsructor, initializes the socket
     */
    public ConnectionManager() {
        log.info("Logger Name: "+log.getName());
        try {
            log.info("Creating Server Socket");
            log.config(address.toString() + " " + Integer.toString(port));
            serverSocket = new ServerSocket(port,backlog,address);
        } catch (IOException e) {
            log.log(Level.SEVERE,"Failed creation serverSocket\n" + e.toString(), e);
            System.out.println("Failed creation serverSocket");
            e.printStackTrace();
        }
    }

}
