package marco.rcl.simpleserver;

import marco.rcl.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * this class manages callbacks
 */
public class CallbackManager extends RemoteObject implements ServerCallbackManager {

    private final static long serialVersionUID = 1L;
    private FriendManager friendManager = null;
    private UserManager userManager = null;
    private ConcurrentHashMap<String,ArrayList<String>> followers;
    private ConcurrentHashMap<String,ArrayList<String>> pendingContents;
    private static final Logger log = Server.getLog();

    /**
     * @param friendManager friend manager of the server
     * @param userManager user manager of the server
     * @param config config parameters
     */
    public CallbackManager(FriendManager friendManager, UserManager userManager, Configs config) {
        this.friendManager = friendManager;
        this.userManager = userManager;
        this.followers = DiskManager.RestoreFriendList(config.CallbackFileName);
        this.pendingContents = DiskManager.RestoreFriendList(config.PendingContents);
        if (followers == null){
            log.severe("problems in restoring callback file");
            throw new RuntimeException("error in restoring callback file, aborting");
        }
        if (pendingContents == null){
            log.severe("problems in restoring pending contents file");
            throw new RuntimeException("error in restoring pending contents file, aborting");
        }
        log.info("callback manager correctly started");
    }

    /**
     * util function creates and allocates an arraylist
     * @param s string to add
     * @return a new arraylist containing the string s
     */
    private ArrayList<String> createAndAdd(String s){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(s);
        return tmp;
    }

    /**
     *
     * @param name
     * @param content
     */
    public void publish(String name, String content){
        String[] friends = friendManager.getFriendList(name);
        if (friends==null) return;
        for (String friend : friends) {
            ClientCallback c = userManager.getCallback(friend);
            if (c!=null) try {
                c.content(content);
                Arrays.asList(friends).remove(friend);
            } catch (RemoteException e) {
                e.printStackTrace();
                log.info("failed sending content to the user " + friend);
            }
        }
        if (friends.length > 0){
            for (String friend : friends) {
                if (pendingContents.containsKey(friend)) pendingContents.get(friend).add(content);
                else pendingContents.put(friend,createAndAdd(content));
            }
        }
    }

    @Override
    public int register(ClientCallback c, String name, String password, Token token) throws RemoteException {
        int check = userManager.setCallback(c, name, password, token);
        if (check == Errors.noErrors){
            if (pendingContents.containsKey(name)){
                ArrayList<String> contents = pendingContents.get(name);
                for (String content : contents) {
                    try {
                        c.content(content);
                        contents.remove(content);
                    } catch (RemoteException e){
                        log.severe("problem in sending contents");
                    }
                }
                if (contents.isEmpty()) pendingContents.remove(name);
            }
        }
        return check;
    }


    @Override
    public int follow(String friendName, String name, String password, Token token) throws RemoteException {
        int check = userManager.checkUser(name, password, token);
        if (check != Errors.noErrors) return check;
        if (Arrays.asList(friendManager.getFriendList(name)).contains(friendName)){
            if (followers.containsKey(friendName)) followers.get(friendName).add(name);
            else followers.put(friendName,createAndAdd(name));
            return Errors.noErrors;
        }
        return Errors.UserNotValid;
    }
}
