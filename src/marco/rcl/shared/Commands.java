package marco.rcl.shared;

/**
 * Created by marko on 09/06/2016.
 */
    /**
     * This interface contains all the possible commands shared between client/server.
     */
public enum Commands {
        Register, Login, SearchUser, FriendRequest, FriendConfirm, FriendIgnore, FriendList, Logout, Publish,
        PendingRequests
}
