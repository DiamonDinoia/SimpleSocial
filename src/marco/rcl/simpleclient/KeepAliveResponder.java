package marco.rcl.simpleclient;

import marco.rcl.shared.Configs;
import marco.rcl.shared.KeepAlive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Created by marko on 06/06/2016.
 */
public class KeepAliveResponder {

    private String keepAliveMessage;
    private MulticastSocket multicast;
    private DatagramSocket client;
    private DatagramPacket keepAlivePacket;
    private DatagramPacket response;
    private ExecutorService ex;
    private boolean responding = false;
    private String name;
    private InetAddress address;
    private int port;
    private final static Logger log = Client.getLog();
    private String password;
    /**
     * constructor tries to set everything up and if something goes wrong exits
     * @param param config param structure
     * @param ex executor service
     * @param name user name
     */
    public KeepAliveResponder(Configs param, ExecutorService ex, String name, String password) {
        try {
            InetAddress group = InetAddress.getByName(param.MulticastGroup);
            multicast = new MulticastSocket((int) param.MulticastPort);
            multicast.setReuseAddress(true);
            multicast.joinGroup(group);
            keepAliveMessage = param.KeepAliveMessage;
            byte[] buf = new byte[keepAliveMessage.getBytes().length];
            keepAlivePacket = new DatagramPacket(buf, keepAliveMessage.getBytes().length);
            client = new DatagramSocket(0);
            client.setReuseAddress(true);
            this.address = InetAddress.getByName(param.DatagramServerAddress);
        } catch (IOException e) {
            log.severe("Impossible to start KeepAliveManager " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.name = name;
        this.ex = ex;
        this.port = (int) param.DatagramServerPort;
        this.password = password;
        log.info("keep alive responder correctly started");
    }

    /**
     * this function initiates the keepaliveresponder to send keepalive responses
     */
    public void startResponding(){
        if (responding) return;
        responding = true;
        log.info("Started responding");
        Random random = new Random();
        ex.submit(() -> {
           try {
               while (responding){
                   multicast.receive(keepAlivePacket);
                   // if arrived the correct request
                   String received = new String(keepAlivePacket.getData());
                   if (! received.equals(keepAliveMessage)) continue;
                   // encode the response
                   byte[] message = KeepAlive.encodeMessage(name,password);
                   response = new DatagramPacket(message,message.length,address,port);
                   // ind order to not flood the server with responses wait a small amount of time
                   Thread.sleep(random.nextInt(100));
                   client.send(response);
                   log.info("response sent");
               }
               // if something goes wrong log and terminate
           } catch (IOException | InterruptedException e) {
               log.severe("something went wrong during response " + e.toString());
               e.printStackTrace();
               throw new RuntimeException(e);
           }
        });
    }

    /**
     * this function tells to the keep alive responder to stop
     */
    public void stopResponding(){
        responding = false;
    }

    public void close(){
        multicast.close();
        client.close();
    }
}
