package marco.rcl.simpleclient;

import marco.rcl.shared.Command;
import marco.rcl.shared.Configs;
import marco.rcl.shared.Response;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * thia class handles the tcp connections between Server and Client
 */
public class TCPHandler {

    private Vector<String> friendsRequests;
    private Socket socket = null;
    private ServerSocket server = null;
    private static final Logger log = Client.getLog();
    private ExecutorService ex;
    private boolean receiving = false;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private int port;
    private String address;


    public TCPHandler(Configs config, Vector<String> friendsRequests, ExecutorService ex) {
        this.friendsRequests = friendsRequests;
        this.ex = ex;
        this.port = (int) config.ServerPort;
        this.address = config.ServerAddress;
        try {
            server = new ServerSocket(0);
            log.info("TCP Handles correctly started");
        } catch (IOException e) {
            log.severe("problems starting TCP handler " + e.toString());
            e.printStackTrace();
            this.close();
            throw new RuntimeException(e);
        }
    }

    /**
     * this function return the address of the client
     * @return the address fo the client
     */
    public String getAddress(){
        return server.getInetAddress().getHostAddress();
    }

    /**
     * this function return the port of the client
     * @return client port
     */
    public int getPort(){
        return server.getLocalPort();
    }

    /**
     * close everything and exits
     */
    public void close(){
        if (socket!=null) try {socket.close();} catch (IOException ignored) {}
        if (server!=null) try {server.close();} catch (IOException ignored) {}
        if (in!=null) try {in.close();} catch (IOException ignored) {}
        if (out!=null) try {out.close();} catch (IOException ignored) {}
    }

    /**
     * start receiving connection from the server
     */
    public void startReceiving(){
        if (receiving) return;
        receiving = true;
        log.info("TCP manager started receiving");
        ex.submit(() -> {
           while (receiving) {
               try (
                       Socket connection = server.accept();
                       BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
               ) {
                   String tmp = reader.readLine();
                   if (tmp != null) friendsRequests.add(tmp);
               } catch (SocketException e){
                   log.info("connection closed");
               }catch (IOException e) {
                   log.severe("something went wrong in receiving server connections");
                   e.printStackTrace();
                   this.close();
                   throw new RuntimeException(e);
               }
           }
        });
    }

    /**
     * stop receiving connections from the server
     */
    public void stopReceiving(){
        receiving = false;
        log.info("TCP manager stopped receiving");
    }

    /**
     * this function sends a command to the server
     * @param command command to send
     * @return returns true if something went wrong or false otherwise
     */
    public boolean sendCommand(Command command){
        try {
            socket = new Socket(address,port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(command);
            out.flush();
            log.info("command sent");
            return false;
        } catch (IOException e) {
            log.severe("problem sending command " + e.toString());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * this function gets the response from the server
     * @return if error null else the response
     */
    public Response getResponse(){
        try {
            in = new ObjectInputStream(socket.getInputStream());
            Response r = (Response) in.readObject();
            in.close();
            out.close();
            return r;
        // empty answer
        } catch (EOFException e){
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.severe("something went wrong in getting response " + e.toString());
            return null;
        }
        return null;
    }
}
