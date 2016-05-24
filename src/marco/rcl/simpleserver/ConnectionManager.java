package marco.rcl.simpleserver;

import marco.rcl.shared.ConnectionParam;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * This class is used to manager the TCP connection with the clients
 */
public class ConnectionManager {
    private ServerSocket serverSocket;
    private int port = ConnectionParam.PORT;
    private InetAddress address = ConnectionParam.ADDRESS;
    private int backlog = 100;
    Logger log = null;

    /**
     * Contsructor, initializes the socket
     */
    public ConnectionManager(Logger log) {
        try {
            this.log = log;
            serverSocket = new ServerSocket(port, backlog, address);
            log.info("creating serverSocket");
            log.severe("test");
        } catch (IOException e) {
            log.severe("Failed creation serverSocket " + e.toString());
            e.printStackTrace();
        }
    }

}
