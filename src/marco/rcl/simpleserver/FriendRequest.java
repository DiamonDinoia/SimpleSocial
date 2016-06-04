package marco.rcl.simpleserver;

/**
 * Created by marko on 04/06/2016.
 */
public class FriendRequest {

    private final String user;
    private final long timestamp;

    public FriendRequest(String user) {
        this.user = user;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
