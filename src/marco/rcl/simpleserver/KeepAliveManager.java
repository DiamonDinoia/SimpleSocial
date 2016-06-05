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
    private boolean receiving = false;
    private int threadsNumber = 0;
    private long maxPacketLength = 96;
    private int receivingBufferSize;
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
            receivingBufferSize = server.getReceiveBufferSize();
        } catch (IOException e) {
            log.severe("Impossible to start KeepAliveManager " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("KeepAliveManager correctly started");
        maxPacketLength = param.MaxPacketLength;
        onlineUsers = new ConcurrentSkipListSet<>();
    }

    /**
     * function used to terminate the process
     */
    public void stopUpdatingStatus(){
        receiving = false;
        computing = false;
    }

    /**
     * cleanup everything and exit
     */
    public void close(){
        this.server.close();
        this.multicast.close();
    }

    /**
     * function called in order to receive datagram packet, checks if the response is arrived in time and then
     * updates the list
     */
    private void receivingTask(int index){
        DatagramPacket dp = new DatagramPacket(new byte[96],96);
            try {
                while (receiving && flags[index]){
                    server.receive(dp);
                    String[] message = KeepAlive.decodeMessage(dp.getData());
                    if (new Long(message[1]) - System.currentTimeMillis() <= TimeUnit.SECONDS.toMillis(10)){
                        onlineUsers.add(message[0]);
                    }
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
     * Because UDP has finite buffer, in case too much datagram packets arrives someone can get lost. To prevent this
     * I try to speedup readings from it in order to minimize packet loss. To achieve this I calculate I pseudo-optimal
     * number of receiving threads and if needed then try to start or terminate them.
     */
    private void setReceivingThreads(){
        // calculating the "optimal" number of threads
        int threads = (int)((3 * onlineUsers.size() * maxPacketLength )/ receivingBufferSize)+1;
        // if the running threads are optimal return
        if (threadsNumber == threads || threads > flags.length) return;
        // too few threads, starting
        if (threadsNumber < threads)
            for ( ; threadsNumber<threads; threadsNumber++ ){
                flags[threadsNumber] = true;
                ex.submit(() -> receivingTask(threadsNumber));
            }
        // too much, terminating
        else for (; threadsNumber>threads; threads--) flags[threadsNumber]=false;
    }


    /**
     * this function tells to start computing keep-alive request on the multicast
     */
    public void startUpdatingStatus(){
       // if already started do nothing
        if (computing) return;
        computing = true;
        ex.submit(() -> {
            try {
                while (computing){
                    // check if the receiving threads are enough
                    setReceivingThreads();
                    // send the keep-alive request in multicast
                    multicast.send(keepAlivePacket);
                    // wait 10 seconds
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));

                    users.forEach((name,value) -> {
                        if (!onlineUsers.contains(name)) value.setOffLine();
                    });
                }
            } catch (IOException | InterruptedException e) {
                log.severe("problems in computing KeepAlive messages " + e.toString());
                e.printStackTrace();
                stopUpdatingStatus();
                close();
            }
        });
    }


}
