package marco.rcl.shared;

/**
 * This interface contains all the possible commands shared between client/server.
 *
 */
public interface Commands {

    int Register = 1;
    int Login = 2;
    int SearchUser = 3;
    int FriendRequest = 4;
    int FriendConfirm = 5;
    int FriendList = 6;


}

