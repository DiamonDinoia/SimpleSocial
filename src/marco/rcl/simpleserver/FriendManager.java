package marco.rcl.simpleserver;


import marco.rcl.shared.Configs;
import marco.rcl.shared.Errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marko on 04/06/2016.
 */

public class FriendManager {

    private final long requestValidity;

    private ConcurrentHashMap<String,HashMap<String,Long>> pendingRequests;
    private ConcurrentHashMap<String,ArrayList<String>> friendships;

    /**
     * constrictor, initializes data structure and restore the status from the disk
     * @param param structure of configuration parameters
     */
    public FriendManager(Configs param) {
        friendships = DiskManager.RestoreFriendList(param.FriendshipFile);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.requestValidity = param.RequestValidity;
        if (friendships == null)
            throw new RuntimeException("problems during frienship restore");
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
        if (receiver.equals(sender)) return Errors.RequestNotValid;
        if (friendships.containsKey(receiver) && friendships.get(receiver).contains(sender))
            return Errors.RequestNotValid;
        if (pendingRequests.containsKey(receiver)){
            pendingRequests.get(receiver).put(sender, System.currentTimeMillis());
        } else {
            HashMap<String,Long> tmp = new HashMap<>();
            tmp.put(sender,System.currentTimeMillis());
            pendingRequests.put(receiver,tmp);
        }
        return Errors.noErrors;
    }

    /**
     * creates a friendship between user u1 and user u2
     * @param u1 a registered user
     * @param u2 the new friend of u1
     */
    private void addFriendship(String u1, String u2){
        if (friendships.contains(u1)){
            friendships.get(u1).add(u2);
        } else {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(u2);
            friendships.put(u1,tmp);
        }
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
            return Errors.noErrors;
        }
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
        // if the receiver has requests and one of them is from the sender then confirm else return error
        if (pendingRequests.containsKey(receiver) && pendingRequests.get(receiver).containsKey(sender)){
            removePendingRequest(receiver,sender);
            return Errors.noErrors;
        }
        return Errors.IgnoreNotValid;
    }

    /**
     * this function deletes all the expired requests, written in functional checks all'timestamps and if there's
     * someone expired then deletes
     */
    public void removeExpiredRequests(){
        pendingRequests.values().removeIf(
                (requestHashMap) -> {
                    requestHashMap.values().removeIf( (timestamp) ->
                            ((System.currentTimeMillis() - timestamp) > requestValidity));
                    return requestHashMap.isEmpty();
                });
    }

    /**
     * this functions return the friendList of the user name, or null if the user has no friends
     * @param name name of the owner of the friendList
     * @return null or the friendList
     */
    public String[] getFriendList(String name){
        return (String[]) friendships.get(name).toArray();
    }

    /**
     * this functions removes the expires requests and returns the remaining or null if there are not
     * @param name name of the owner of the pending requests
     * @return null or pending request array
     */
    public String[] getPendingRequests(String name){
        if (!pendingRequests.containsKey(name)) return null;
        pendingRequests.get(name).values().removeIf( (timestamp) ->
            System.currentTimeMillis() - timestamp > requestValidity);
        return (String[]) Collections.list(pendingRequests.keys()).toArray();
    }

}
