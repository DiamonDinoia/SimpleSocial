package marco.rcl.simpleserver;

import marco.rcl.shared.Configs;
import marco.rcl.shared.KeepAlive;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/** This class is used to handle keep-alive transmissions
 * Created by marko on 03/06/2016.
 */
public class KeepAliveManager {

    private ConcurrentHashMap<String,User> users;
    private MulticastSocket multicast;
    private DatagramPacket keepAlivePacket;
    private ExecutorService ex;
    private final static Logger log = Server.getLog();
    private boolean computing = false;
    private DatagramSocket server;
    private ConcurrentSkipListSet<String> onlineUsers;

    private boolean[] flags = new boolean[512];

    /**
     * @param users Registered user list
     * @param param Configuration parameters
     * @param ex ExecutorService in order to use only one in the server
     */
    public KeepAliveManager(ConcurrentHashMap<String, User> users, Configs param, ExecutorService ex) {
        this.users = users;
        this.ex = ex;
        try {
            // create multicast and receiving socket
            InetAddress group = InetAddress.getByName(param.MulticastGroup);
            multicast = new MulticastSocket((int) param.MulticastPort);
            multicast.setReuseAddress(true);
            //probably I don't need to join if I only send messages
            //multicast.joinGroup(group);
            keepAlivePacket = new DatagramPacket(param.KeepAliveMessage.getBytes(),
                    param.KeepAliveMessage.length(), group, (int) param.MulticastPort);
            server = new DatagramSocket((int) param.DatagramServerPort,
                    InetAddress.getByName(param.DatagramServerAddress));
            server.setReuseAddress(true);
        } catch (IOException e) {
            log.severe("Impossible to start KeepAliveManager " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("KeepAliveManager correctly started");
        onlineUsers = new ConcurrentSkipListSet<>();
    }

    /**
     * function used to terminate the process
     */
    public void stopUpdatingStatus(){
        computing = false;
        log.info("keep alive stopped updating status");
    }

    /**
     * cleanup everything and exit
     */
    public void close(){
        this.server.close();
        this.multicast.close();
        log.info("keep alive closed connections");
    }

    /**
     * function called in order to receive datagram packet, checks if the response is arrived in time and then
     * updates the list
     */
    private void receivingTask(){
        DatagramPacket dp = new DatagramPacket(new byte[96],96);
            try {
                while (computing){
                    server.receive(dp);
                    String[] message = KeepAlive.decodeMessage(dp.getData());
                    // if the response arrived in time, the user is registered and is online then accept else skip the
                    // response
                    if ((System.currentTimeMillis() - new Long(message[2])) <= TimeUnit.SECONDS.toMillis(10)){
                        onlineUsers.add(message[0]);
                        log.info("user online");
                    }
                    log.info( Thread.currentThread().getName() + ": received a packet from user: " + message[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopUpdatingStatus();
                close();
                log.severe("this should not happen in receiving task " + e.toString());
                throw new RuntimeException(e);
        }
    }

    /**
     * this function notifies to the keep-alive manager the new user logged or registered
     * @param user new user added to online check
     */
    void notify(String user){
        onlineUsers.add(user);
    }



    /**
     * this function tells to start computing keep-alive request on the multicast
     * Because I expect that the online users a large number. I try to create a good number of receivers threads.
     * Because UDP ha finite buffer and if it is full datagram gets lost.
     * TODO: allow the userManager to put new Users to the onlineUsers list
     */
    public void startUpdatingStatus(){
       // if already started do nothing
        if (computing) return;
        computing = true;
        log.info("started updating status");
        ex.submit(() -> {
            try {
                int cores = Runtime.getRuntime().availableProcessors();
                log.info("starting " + Integer.toString(cores) + " UDP receivers tasks" );
                for (int i=0; i < cores; i++){
                    ex.submit(this::receivingTask);
                }
                while (computing){
                    // check if the receiving threads are enough
                    // send the keep-alive request in multicast
                    multicast.send(keepAlivePacket);
                    // wait 10 seconds
                    log.info("multicast message sent");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(12));
                    users.forEach((name,value) -> {
                        if (!onlineUsers.contains(name)) value.setOffLine();
                        else onlineUsers.remove(name);
                    });
                }
            } catch (IOException | InterruptedException e) {
                log.severe("problems in computing KeepAlive messages " + e.toString());
                e.printStackTrace();
                stopUpdatingStatus();
                close();
                throw new RuntimeException(e);
            }
        });
    }


}
