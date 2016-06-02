package marco.rcl.shared;

/**
 * Created by marko on 03/06/2016.
 */
public class UserShared {
    private final String name;
    private final boolean status;

    public UserShared(String name, boolean status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public boolean isStatus() {
        return status;
    }
}
