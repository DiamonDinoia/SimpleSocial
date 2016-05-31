package marco.rcl.simpleserver;

import marco.rcl.shared.ConnectionParam;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
    private ExecutorService executorService;
    private boolean accept = false;
    private LinkedBlockingQueue<Socket> connections;

    /**
     * Contsructor, initializes the socket
     */
    public ConnectionManager(Logger log) {
        try {
            this.log = log;
            serverSocket = new ServerSocket(port, backlog, address);
            log.info("created serverSocket");
            executorService = Executors.newCachedThreadPool();
        } catch (IOException e) {
            log.severe("Failed creation serverSocket " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private void startAccepting(){
        // inner class, used to implements lexical closures
        abstract class ClientAccepter implements Runnable{}
        executorService.execute(new ClientAccepter() {
            @Override
            public void run() {
                try {
                    while (accept) {
                        try {
                            connections.put(serverSocket.accept());
                        } catch (InterruptedException e) {
                            log.severe("Problems in accepting clients " + e.toString());
                            e.printStackTrace();
                        }
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

    private void dispatcher(){
        abstract class CommandResponder implements Runnable{};
        executorService.submit(new CommandResponder() {
            @Override
            public void run() {
                while (accept){
                    try {
                        Socket socket = connections.take();

                    } catch (InterruptedException e) {
                        log.severe("failed taking from the queue" + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Initiates the ConnectionManager to accept new connections
     */
    public void startManagingConnections(){
        accept=true;
        startAccepting();
        dispatcher();
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
        executorService.shutdown();
    }
}
