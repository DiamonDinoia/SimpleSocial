package marco.rcl.simpleServer;

import marco.rcl.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static marco.rcl.shared.Errors.UserNotValid;
import static marco.rcl.shared.Errors.noErrors;

/**
 * this class manages callbacks from and to the clients
 */
public class CallbackManager extends RemoteObject implements ServerCallbackManager {

    private final static long serialVersionUID = 1L;
    private FriendManager friendManager = null;
    private UserManager userManager = null;
    private ConcurrentHashMap<String, ArrayList<String>> followers;
    private ConcurrentHashMap<String, ArrayList<String>> pendingContents;
    private static final Logger log = Server.getLog();
    private String followersFile;
    private String pendingContentsFile;
    private long backupInterval;
    private boolean backing = false;
    private ExecutorService ex = Server.getExecutorService();

    /**
     * @param friendManager friend manager of the server
     * @param userManager   user manager of the server
     * @param config        config parameters
     */
    public CallbackManager(FriendManager friendManager, UserManager userManager, Configs config) {
        this.friendManager = friendManager;
        this.userManager = userManager;
        this.pendingContentsFile = config.PendingContents;
        this.followersFile = config.FollowersFileName;
        this.backupInterval = config.BackupInterval;
        this.followers = DiskManager.RestoreFromDisk(config.FollowersFileName);
        // if something goes wrong there nothing there's no recovery, aborting
        if (followers == null) {
            log.severe("problems in restoring callback file");
            throw new RuntimeException("error in restoring callback file, aborting");
        }
        // if something goes wrong there nothing there's no recovery, aborting
        this.pendingContents = DiskManager.RestoreFromDisk(config.PendingContents);
        if (pendingContents == null) {
            log.severe("problems in restoring pending contents file");
            throw new RuntimeException("error in restoring pending contents file, aborting");
        }
        log.info("callback manager correctly started");
    }

    /**
     * util function that creates and allocates an arrayList
     * @param s string to add
     * @return a new arrayList containing the string s
     */
    private ArrayList<String> createAndAdd(String s) {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(s);
        return tmp;
    }


    /**
     * this function send the contents to the followers by their callbacks
     * @param user    the publisher
     * @param content content published
     */
    public synchronized void publish(String user, String content) {
        // get the followers of the publisher
        ArrayList<String> userFollowers = followers.get(user);
        // if the user has no followers
        if (userFollowers == null) return;
        // if the user has followers
        // for every follower
        for (String follower : userFollowers) {
            boolean backup = false;
            ClientCallback c = userManager.getCallback(follower);
            if (!userManager.getUsers().get(follower).isOnline() || c == null) backup = true;
            else {
                try {
                    // try to send him the content
                    c.content(user + ": " + content);
                    log.info("content correctly sent to " + user);
                    // if ok go to the next follower
                } catch (RemoteException e) {
                    backup = true;
                    e.printStackTrace();
                    log.info("failed sending content to the user " + follower);
                }
            }
            // if the follower is offline or the send fails save the pending content
            if (backup) {
                if (pendingContents.containsKey(follower)) pendingContents.get(follower).add(user + ": " + content);
                else pendingContents.put(follower, createAndAdd(user + ": " + content));
            }
        }
    }

    /**
     * this function allows user to register their callbacks
     *
     * @param c        user callback
     * @param name     user name
     * @param password user password
     * @param token    user token, to validate the connection
     * @return Error or confirm message
     */
    @Override
    public synchronized Errors register(ClientCallback c, String name, String password, Token token) throws RemoteException {
        // check if the user is valid and set his callback
        Errors check = userManager.setCallback(c, name, password, token);
        // if there are no errors and the user has pending content, try send him
        if (check == noErrors) {
            if (pendingContents.containsKey(name)) {
                boolean flag = true;
                Vector<String> contents = new Vector<>(pendingContents.get(name));
                try {
                    for (String content : contents) {
                        c.content(content);
                    }
                } catch (RemoteException e) {
                    log.severe("problem in sending contents" + e.toString());
                    flag = false;
                }
                if (flag) pendingContents.remove(name);
            }
        }
        log.info("user correctly " + name + " registered");
        return check;
    }

    /**
     * this function is used by the user to follow one of their friend
     *
     * @param friendName friend name
     * @param name       user name
     * @param password   user password
     * @param token      token to validate the connection
     * @return Error or confirm message
     */
    @Override
    public synchronized Errors follow(String friendName, String name, String password, Token token) throws RemoteException {
        // if the user is valid
        Errors check = userManager.checkUser(name, password, token);
        if (check != noErrors) return check;
        // a user can follow only one of his friends
        if (friendManager.getFriendList(name) == null) return UserNotValid;
        if (Arrays.asList(friendManager.getFriendList(name)).contains(friendName)) {
            if (followers.containsKey(friendName)) {
                if (followers.get(friendName).contains(name)) return UserNotValid;
                followers.get(friendName).add(name);
            } else followers.put(friendName, createAndAdd(name));
            return noErrors;
        }
        return UserNotValid;
    }

    /**
     * this function saves the current state of followers and contents on the disk
     */
    public void startBackup() {
        // if already started do noting
        if (backing) return;
        backing = true;
        // loops forever saving the structures
        ex.submit(() -> {
            try {
                while (backing) {
                    if (DiskManager.saveToDisk(followers, followersFile)) {
                        log.severe("failed saving follower list on the disk");
                        throw new RuntimeException("failed saving follower list on the disk");
                    }
                    if (DiskManager.saveToDisk(pendingContents, pendingContentsFile)) {
                        log.severe("failed saving pending contents on the disk");
                        throw new RuntimeException("failed saving pending contents on the disk");
                    }
                    log.info("backup complete");
                    // because disk is slow do this only after an interval
                    Thread.sleep(backupInterval);
                }
            } catch (InterruptedException e) {
                log.severe("CallbackManager interrupted");
            }
        });
    }

    /**
     * this function stops the backup to the disk and terminates the threads
     */
    public void stopBackup() {
        backing = false;
        Thread.currentThread().interrupt();
    }
}
