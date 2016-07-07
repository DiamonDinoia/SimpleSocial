package marco.rcl.simpleserver;

import marco.rcl.shared.Configs;
import marco.rcl.shared.KeepAlive;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 * This class is used to handle keep-alive transmissions
 */
public class KeepAliveManager {

    private ConcurrentHashMap<String, User> users;
    private MulticastSocket multicast;
    private DatagramPacket keepAlivePacket;
    private ExecutorService ex = Server.getExecutorService();
    private final static Logger log = Server.getLog();
    private boolean computing = false;
    private DatagramSocket server;
    private ConcurrentSkipListSet<String> onlineUsers;
    private LinkedBlockingQueue<byte[]> queue;
    private int maxLength;
    /**
     * @param users Registered user list
     * @param param Configuration parameters
     */
    public KeepAliveManager(ConcurrentHashMap<String, User> users, Configs param) {
        this.users = users;
        try {
            // create multicast and receiving socket
            InetAddress group = InetAddress.getByName(param.MulticastGroup);
            multicast = new MulticastSocket((int) param.MulticastPort);
            multicast.setReuseAddress(true);
            //probably I don't need to join if I only send messages
            //multicast.joinGroup(group);
            keepAlivePacket = new DatagramPacket(param.KeepAliveMessage.getBytes(),
                    param.KeepAliveMessage.length(), group, (int) param.MulticastPort);
            server = new DatagramSocket(null);
            SocketAddress sa = new InetSocketAddress(InetAddress.getByName(param.DatagramServerAddress),
                    (int) param.DatagramServerPort);
            server.bind(sa);
            server.setReuseAddress(true);
        } catch (IOException e) {
            log.severe("Impossible to start KeepAliveManager " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("KeepAliveManager correctly started");
        onlineUsers = new ConcurrentSkipListSet<>();
        queue = new LinkedBlockingQueue<>();
        this.maxLength = (int) param.MaxPacketLength;
    }

    /**
     * function used to terminate the process
     */
    public void stopUpdatingStatus() {
        computing = false;
        log.info("keep alive stopped updating status");
        Thread.currentThread().interrupt();
    }

    /**
     * cleanup everything and exit
     */
    public void close() {
        this.stopUpdatingStatus();
        Thread.currentThread().interrupt();
        this.server.close();
        this.multicast.close();
        log.info("keep alive closed connections");
    }

    /**
     * this functions decodes the keep-alive packet and updates the onlineUser list
     */
    private void decodingTask() {
        try {
            while (computing) {
                byte[] tmp = queue.take();
                String[] message = KeepAlive.decodeMessage(tmp);
                // if the user is valid
                User user = users.get(message[0]);
                if (user == null) continue;
                if (!user.getPassword().equals(message[1])) continue;
                // if the response arrived in time, the user is registered and is online then accept else skip the
                // response
                if ((System.currentTimeMillis() - new Long(message[2])) <= TimeUnit.SECONDS.toMillis(10)) {
                    onlineUsers.add(message[0]);
                    log.info("user: " + message[0] + " online");
                } else {
                    log.info("user: " + message[0] + " offline");

                }
            }
        } catch (InterruptedException e) {
            log.info("keepAliveMessenger interrupted " + e.toString());
        }
    }

    /**
     * function called in order to receive and send to the decoder the datagram packet
     */
    private void receivingTask() {
        DatagramPacket dp = new DatagramPacket(new byte[maxLength], maxLength);
        try {
            while (computing) {
                server.receive(dp);
                queue.put(dp.getData());
                dp.setLength(maxLength);
            }
        } catch (IOException e) {
            if (computing) {
                close();
                log.severe("Problems receiving Keep-alive responses " + e.toString());
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            log.info("keepAliveManager interrupted");
        }
    }

    /**
     * this function notifies to the keep-alive manager the new user logged or registered
     *
     * @param user new user added to online check
     */
    void notify(String user) {
        onlineUsers.add(user);
    }


    /**
     * TODO: uses int to set user status in order to not use the online user list
     * this function tells to start computing keep-alive request on the multicast
     * Because I expect that the online users a large number. I try to create a good number of receivers threads.
     * Because UDP ha finite buffer and if it is full datagrams gets lost.
     */
    public void startUpdatingStatus() {
        // if already started do nothing
        if (computing) return;
        computing = true;
        log.info("started updating status");
        ex.submit(() -> {
            try {
                //creates one thread for each core on the PC
                int cores = Runtime.getRuntime().availableProcessors();
                log.info("starting " + Integer.toString(cores) + " UDP receivers tasks");
                for (int i = 0; i < cores; i++) ex.submit(this::receivingTask);
                for (int i = 0; i < (cores + 1) / 2; i++) ex.submit(this::decodingTask);
                while (computing) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    // check if the receiving threads are enough
                    // send the keep-alive request in multicast
                    multicast.send(keepAlivePacket);
                    // wait 10 seconds
                    log.info("multicast message sent");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(12));
                    users.forEach((name, value) -> {
                        if (!onlineUsers.contains(name)) value.setOffLine();
                    });
                    onlineUsers.clear();
                }
            } catch (IOException e) {
                log.severe("problems in computing KeepAlive messages " + e.toString());
                close();
            } catch (InterruptedException e) {
                log.info("keepAliveManager interrupted");
            }
        });
    }


}
