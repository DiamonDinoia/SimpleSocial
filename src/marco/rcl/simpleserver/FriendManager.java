package marco.rcl.simpleserver;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marko on 04/06/2016.
 */



public class FriendManager {

    private final String pendingRequestFile;
    private final String friendshipFile;

    private ConcurrentHashMap<String,ArrayList<FriendRequest>> pendingRequests;
    private ConcurrentHashMap<String,ArrayList<String>> friendships;

    public FriendManager(String friendListFile, String pendingRequestFile, String friendshipFile ) {
        this.pendingRequestFile = pendingRequestFile;
        this.friendshipFile = friendshipFile;
        pendingRequests = DiskManager.RestorePendingRequest(pendingRequestFile);
        friendships = DiskManager.RestoreFriendList(friendListFile);
    }


}
