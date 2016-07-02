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
    private InetAddress address;
    private int port;
    private final static Logger log = Client.getLog();
    /**
     * constructor tries to set everything up and if something goes wrong exits
     * @param param config param structure
     */
    public KeepAliveResponder(Configs param) {
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
        this.port = (int) param.DatagramServerPort;
        ex = Client.getExecutorService();
        log.info("keep alive responder correctly started");
    }

    /**
     * this function initiates the keepaliveresponder to send keepalive responses
     */
    public void startResponding(String name, String password){
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
                   // in order to not flood the server with responses wait a small amount of time
                   Thread.sleep(random.nextInt(2000));
                   client.send(response);
                   log.info("response sent");
               }
               // if something goes wrong log and terminate
           } catch (IOException | InterruptedException e) {
              if (responding) {
                  log.severe("something went wrong during response " + e.toString());
                  e.printStackTrace();
                  throw new RuntimeException(e);
              }
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
        stopResponding();
        multicast.close();
        client.close();
        Thread.currentThread().interrupt();
    }
}
