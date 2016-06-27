package marco.rcl.shared;

import java.io.Serializable;

/**
 * Created by marko on 03/06/2016.
 */
public class UserShared  implements Serializable{

    private final static long serialVersionUID = 1L;

    private final String name;
    private final boolean status;

    public UserShared(String name, boolean status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return status;
    }

    @Override
    public String toString() {
        return name + " " + status;
    }
}
