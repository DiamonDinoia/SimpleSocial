package marco.rcl.simpleserver;

import marco.rcl.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ConcurrentHashMap<String,ArrayList<String>> followers;
    private ConcurrentHashMap<String,ArrayList<String>> pendingContents;
    private static final Logger log = Server.getLog();
    private String followersFile;
    private String pendingContentsFile;
    private long backupInterval;
    private boolean backing = false;
    private ExecutorService ex = null;

    /**
     * @param friendManager friend manager of the server
     * @param userManager user manager of the server
     * @param config config parameters
     */
    public CallbackManager(FriendManager friendManager, UserManager userManager, Configs config, ExecutorService ex) {
        this.friendManager = friendManager;
        this.userManager = userManager;
        this.pendingContentsFile = config.PendingContents;
        this.followersFile = config.CallbackFileName;
        this.backupInterval = config.BackupInterval;
        this.ex = ex;
        this.followers = DiskManager.RestoreFriendList(config.CallbackFileName);
        // if something goes wrong there nothing there's no recovery, aborting
        if (followers == null){
            log.severe("problems in restoring callback file");
            throw new RuntimeException("error in restoring callback file, aborting");
        }
        this.pendingContents = DiskManager.RestoreFriendList(config.PendingContents);
        if (pendingContents == null){
            log.severe("problems in restoring pending contents file");
            throw new RuntimeException("error in restoring pending contents file, aborting");
        }
        log.info("callback manager correctly started");
    }

    /**
     * util function creates and allocates an arrayList
     * @param s string to add
     * @return a new arrayList containing the string s
     */
    private ArrayList<String> createAndAdd(String s){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(s);
        return tmp;
    }

    /**
     * this function send the contents to the followers by theru callbacks
     * @param user  the publisher
     * @param content content published
     */
    public void publish(String user, String content){
        // get the followers of the publisher
        ArrayList<String> userFollowers = (ArrayList<String>) followers.get(user);
        // if the user has no followers
        if (userFollowers==null) return;
        // if the user has followers
        // for every follower
        for (String follower : userFollowers) {
            // try to get his callback
            ClientCallback c = userManager.getCallback(follower);
            // if the callback is present
            if (c != null) {
                // try to send him the content
                try {
                    c.content("User: " + user + " Content: " + content + '\n');
                    log.info("content correctly sent to " + user);
                    // if ok go to the next follower
                    continue;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    log.info("failed sending content to the user " + follower);
                }
            }
            // if the follower is offline or the send fails save the pending content
            if (pendingContents.containsKey(follower))
                pendingContents.get(follower).add("User: " + user + " Content: " + content);
            else pendingContents.put(follower, createAndAdd("User: " + user + " Content: " + content));
        }
    }

    @Override
    public Errors register(ClientCallback c, String name, String password, Token token) throws RemoteException {
        // check if the user is valid and set his callback
        Errors check = userManager.setCallback(c, name, password, token);
        // if there are no errors and the user has pending content, try send him
        if (check == noErrors){
            if (pendingContents.containsKey(name)){
                ArrayList<String> contents = pendingContents.get(name);
                for (String content : contents) {
                    try {
                        c.content(content);
                        contents.remove(content);
                    } catch (RemoteException e){
                        log.severe("problem in sending contents" + e.toString());
                    }
                }
                if (contents.isEmpty()) pendingContents.remove(name);
            }
        }
        log.info("user correctly " + name + " registered");
        return check;
    }


    @Override
    public Errors follow(String friendName, String name, String password, Token token) throws RemoteException {
        // if the user is valid
        Errors check = userManager.checkUser(name, password, token);
        if (check != noErrors) return check;
        // a user can follow only one of his friends
        if (Arrays.asList(friendManager.getFriendList(name)).contains(friendName)){
            if (followers.containsKey(friendName)) followers.get(friendName).add(name);
            else followers.put(friendName,createAndAdd(name));
            return noErrors;
        }
        return UserNotValid;
    }

    /**
     * this function saves the current state of followers and contents on the disk
     * @throws InterruptedException
     */
    public void startBackup() {
        // if already started do noting
        if (backing) return;
        backing = true;
        // loops forever saving the structures
        ex.submit(() -> {
            try {
                while (backing){
                    if (DiskManager.dumpFriendList(followers,followersFile)) {
                        log.severe("failed saving follower list on the disk");
                        throw new RuntimeException("failed saving follower list on the disk");
                    }
                    if (DiskManager.dumpFriendList(pendingContents,pendingContentsFile)){
                        log.severe("failed saving pending contents on the disk");
                        throw new RuntimeException("failed saving pending contents on the disk");
                    }
                    log.info("backup complete");
                    // because disk is slow do this only after an interval
                        Thread.sleep(backupInterval);
                }
            } catch (InterruptedException e) {
                this.stopBackup();
                log.severe("problems in saving friendships");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public void stopBackup(){
        backing = false;
    }
}
