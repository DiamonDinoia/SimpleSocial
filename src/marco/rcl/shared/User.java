package marco.rcl.shared;

/**
 * This class is used to represent the user
 */
public class User {
    private final String name;
    private final String password;
    private long token;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

}
