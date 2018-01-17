package marco.rcl.shared;

import java.io.Serializable;

/**
 * This class il used by the server to send to che clients information about their friends
 */
public class UserShared implements Serializable {

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
        return "name: " + name + " status: " + (status ? "on-line" : "off-line");
    }
}
