/**
 * Created by marko on 23/05/2016.
 */


public class Commands {


    public final int Register = 1;
    public final int Login = 2;
    public final int SearchUser = 3;
    public final int FriendRequest = 4;
    public final int FriendConfirm = 5;
    public final int FriendList = 6;

    private long token = -1;

    public long getToken() {
        return token;
    }


}
