package marco.rcl.simpleserver;


import marco.rcl.shared.Configs;
import marco.rcl.shared.Errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * this class manages friendships
 */

public class FriendManager {

    private final long requestValidity;
    private boolean remove = false;
    private boolean dumping = false;
    private long backupInterval;
    private String fileName = null;
    private ConcurrentHashMap<String,HashMap<String,Long>> pendingRequests;
    private ConcurrentHashMap<String,ArrayList<String>> friendships;
    private final static Logger log = Server.getLog();

    /**
     * constrictor, initializes data structure and restore the status from the disk
     * @param param structure of configuration parameters
     */
    public FriendManager(Configs param) {
        friendships = DiskManager.RestoreFriendList(param.FriendshipFile);
        if (friendships == null){
            log.severe("friendships restore failed");
            throw new RuntimeException("problems during friendships restore");
        }
        this.pendingRequests = new ConcurrentHashMap<>();
        this.requestValidity = param.RequestValidity;
        fileName = param.FriendshipFile;
        backupInterval = param.BackupInterval;
        log.info("friend manager correctly started");
    }

    /**
     * this function if the user u already sent a friend request to the user u2 updates the previous request, else adds
     * a new one to the list.
     * if the users are already friends return an error
     * @param receiver the user that receives the request
     * @param sender the user that send the request
     * @return Error code or Confirm
     */
    public int addFriendRequest(String receiver, String sender){
        // if the sender and the receiver are the same return error
        if (receiver.equals(sender)) return Errors.RequestNotValid;
        // if they are already friends return error
        if (friendships.containsKey(receiver) && friendships.get(receiver).contains(sender))
            return Errors.RequestNotValid;
        // else add the friend request
        if (pendingRequests.containsKey(receiver)){
            pendingRequests.get(receiver).put(sender, System.currentTimeMillis());
        } else {
            HashMap<String,Long> tmp = new HashMap<>();
            tmp.put(sender,System.currentTimeMillis());
            pendingRequests.put(receiver,tmp);
        }
        log.info("added a friend request");
        return Errors.noErrors;
    }

    /**
     * creates a friendship between user u1 and user u2
     * @param u1 a registered user
     * @param u2 the new friend of u1
     */
    private void addFriendship(String u1, String u2){
        if (friendships.containsKey(u1)){
            friendships.get(u1).add(u2);
        } else {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(u2);
            friendships.put(u1,tmp);
        }
        log.info("added a friendship");
    }

    /**
     * remove the friendRequest of the sender from the FriendRequests of the receiver
     * @param receiver the receiver of the request
     * @param sender the sender of the request
     */
    private void removePendingRequest(String receiver, String sender){
        HashMap<String,Long> tmp = pendingRequests.get(receiver);
        tmp.remove(sender);
        if (tmp.isEmpty()) pendingRequests.remove(receiver);
        log.info("removed a pending request");
    }

    /**
     * if the receiver has received a friendRequest from the sender then confirm it otherwise send
     * an error message
     * @param receiver the receiver of the request
     * @param sender the sender of the request
     * @return confirm, otherwise an error
     */
    public int confirmFriendRequest(String receiver, String sender){
        // if the receiver has requests and one of them is from the sender then confirm else return error
        if (pendingRequests.containsKey(receiver) && pendingRequests.get(receiver).containsKey(sender)){
            addFriendship(receiver,sender);
            addFriendship(sender,receiver);
            removePendingRequest(receiver,sender);
            log.info("pending request confirmed");
            return Errors.noErrors;
        }
        log.info("impossible to confirm a pending request");
        return Errors.ConfirmNotValid;
    }

    /**
     * if the receiver has received a friendRequest from the sender then remove it otherwise send
     * an error message
     * @param receiver the receiver of the request
     * @param sender the sender of the request
     * @return confirm, otherwise an error
     */
    public int ignoreFriendRequest(String receiver, String sender){
        // if the receiver has requests and one of them is from the sender then ignore else return error
        if (pendingRequests.containsKey(receiver) && pendingRequests.get(receiver).containsKey(sender)){
            removePendingRequest(receiver,sender);
            log.info("pending request correctly ignored");
            return Errors.noErrors;
        }
        log.info("pending request not valid");
        return Errors.IgnoreNotValid;
    }

    /**
     * this function deletes all the expired requests, checks all timestamps and if there's someone expired then deletes
     */
    public void removeExpiredRequests(){
        pendingRequests.values().removeIf(
                (requestHashMap) -> {
                    requestHashMap.values().removeIf( (timestamp) ->
                            ((System.currentTimeMillis() - timestamp) > requestValidity));
                    return requestHashMap.isEmpty();
                });
        log.info("expired requests removed");
    }

    /**
     * this functions return the friendList of the user name, or null if the user has no friends
     * @param name name of the owner of the friendList
     * @return null or the friendList
     */
    public String[] getFriendList(String name){
        log.info("asked a friendlist by " + name);
        return (String[]) friendships.get(name).toArray();
    }

    /**
     * this functions removes the expires requests and returns the remaining or null if there are not
     * @param name name of the owner of the pending requests
     * @return null or pending request array
     */
    public String[] getPendingRequests(String name){
        log.info("asked for pending requests " + name);
        if (!pendingRequests.containsKey(name)) return null;
        pendingRequests.get(name).values().removeIf( (timestamp) ->
            System.currentTimeMillis() - timestamp > requestValidity);
        return (String[]) Collections.list(pendingRequests.keys()).toArray();
    }

    /**
     * this functions initiates the friend manager to compact the pending request list
     * @throws InterruptedException in case of interruption
     */
    public void startRemovingExpiredRequests() throws InterruptedException {
        if (remove) return;
        remove = true;
        log.info("started removing expired requests");
        while (remove) {
            removeExpiredRequests();
            Thread.sleep(requestValidity / 2);
        }
    }

    /**
     * this function tells to the friend manager to stop removing the pending request list
     */
    public void stopRemovingExpiredRequests(){
        remove = false;
        log.info("stopped removing expired requests");
    }

    /**
     * this function tells to the friendManager to start backing-up the friendlist
     * @throws InterruptedException
     */
    public void startDumpingFriendships() throws InterruptedException {
        if (dumping) return;
        log.info("start performing backups");
        dumping = true;
        while (dumping){
            if(DiskManager.dumpFriendList(friendships,fileName)){
                log.severe("failed performing a backup");
                throw new RuntimeException("failed saving friendships exiting");
            }
            log.info("backup complete");
            Thread.sleep(backupInterval);
        }
    }

    /**
     * this function tells to the friendManager to stop backing-up
     */
    public void stopDumpingFriendships(){
        log.info("stopped backup");
        dumping=false;
    }
}
