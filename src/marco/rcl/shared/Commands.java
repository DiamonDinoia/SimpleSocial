package marco.rcl.shared;

/**
 * This interface contains all the possible commands shared between client/server.
 */
public interface Commands {

    int Register = 1;
    int Login = 2;
    int SearchUser = 3;
    int FriendRequest = 4;
    int FriendConfirm = 5;
    int FriendIgnore = 6;
    int FriendList = 7;
    int Logout = 8;
    int Publish = 9;
    int PendingRequests = 10;

}

