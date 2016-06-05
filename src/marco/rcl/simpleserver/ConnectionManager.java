package marco.rcl.simpleserver;

import marco.rcl.shared.Command;
import marco.rcl.shared.Configs;
import marco.rcl.shared.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * This class is used to manager the TCP connection with the clients
 */
public class ConnectionManager {
    private ServerSocket serverSocket;
    private Logger log = Server.getLog();
    private ExecutorService executorService;
    private boolean manage = false;
    private UserManager userManager = null;

    /**
     * Constructor, initializes the socket
     */
    public ConnectionManager(Configs configs, ExecutorService ex , UserManager userManager) {
        try {
            InetAddress address = InetAddress.getByName(configs.ServerAddress);
            serverSocket = new ServerSocket((int)configs.ServerPort, (int)configs.Backlog ,address);
            log.info("created serverSocket");
            executorService = ex;
            this.userManager = userManager;
        } catch (IOException e) {
            log.severe("Failed creation serverSocket " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public void startManaging() {
        if (manage) return;
        manage = true;
        executorService.submit(() -> {
            try {
                while (manage) {
                    Socket socket = serverSocket.accept();
                    executorService.submit(() -> responder(socket));
                    log.info("client accepted");
                }
            } catch (SocketException e) {
                if (manage) {
                    log.severe("Failed dispatcher client " + e.toString());
                    e.printStackTrace();
                } else log.info("stopped dispatcher connections, serverSocket closed");
            } catch (IOException e) {
                log.severe("Failed dispatcher client " + e.toString());
                e.printStackTrace();
            }
        });
    }

    /**
     * this function is used to perform async communications with the users
     * @param socket the connection with the user
     */
    private void responder(Socket socket){
        // it is all in one try block because is not important which one fails, the user can always try another time
        Command command = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            command = (Command) in.readObject();
            Response response = userManager.decodeCommand(command);
            out = new ObjectOutputStream(socket.getOutputStream());
            log.info("user " + command.getName() + "correctly handled");
            out.writeObject(response);
            in.close();
            out.close();
            // simply logs the error, this is not a fatal one
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            log.severe("user " + (command!=null ? command.getName() : "" ) + "not correctly handled " + e.toString() );
            e.printStackTrace();
            // cleanup and return
        } finally {
            try {
                socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * stops the ConnectionManager to manage new clients
     */
    public void stopManaging(){
        manage = false;
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
    }
}
