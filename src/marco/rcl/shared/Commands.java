package marco.rcl.shared;

/**
 * This interface contains all the possible commands shared between client/server.
 *
 */
public interface Commands {

    public final int Register = 1;
    public final int Login = 2;
    public final int SearchUser = 3;
    public final int FriendRequest = 4;
    public final int FriendConfirm = 5;
    public final int FriendList = 6;

}
