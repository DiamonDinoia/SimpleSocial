package marco.rcl.simpleserver;

import marco.rcl.shared.ConnectionParam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This class is used to manager the TCP connection with the clients
 */
public class ConnectionManager {
    private ServerSocket serverSocket;
    private int port = ConnectionParam.PORT;
    private InetAddress address = ConnectionParam.ADDRESS;
    private int backlog = 100;
    private Logger log = null;
    private ExecutorService accepter;
    private boolean accept = false;
    /**
     * Contsructor, initializes the socket
     */
    public ConnectionManager(Logger log) {
        try {
            this.log = log;
            serverSocket = new ServerSocket(port, backlog, address);
            log.info("created serverSocket");
            accepter = Executors.newSingleThreadExecutor();
        } catch (IOException e) {
            log.severe("Failed creation serverSocket " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Initiates the ConnectionManager to accept new connections
     */
    public void startManagingConnections(){
        // inner class, used to implements lexical closures
        abstract class ClientAccepter implements Runnable{}
        accept=true;
        accepter.submit(new ClientAccepter() {
            @Override
            public void run() {
                try {
                    while (accept) {
                        serverSocket.accept();
                        log.info("client accepted");
                    }
                }catch (SocketException e){
                    if (accept){
                        log.severe("Failed accepting client " + e.toString());
                        e.printStackTrace();
                    }
                    else {
                        log.info("stopped accepting connections, serverSocked closed");
                    }
                }catch(IOException e){
                        log.severe("Failed accepting client " + e.toString());
                        e.printStackTrace();
                    }
                }
        });

    }

    /**
     * stops the ConnectionManager to accept new clients
     */
    public void stopManagingConnections(){
        accept=false;
    }

    /**
     * close the connection manager
     */
    public void close(){
        try {
            serverSocket.close();
            log.info("closing connection manager");
        } catch (IOException e) {
            log.severe("error closing connection manager " + e.toString());
            e.printStackTrace();
        }
        accepter.shutdown();
    }
}
