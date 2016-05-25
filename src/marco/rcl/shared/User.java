package marco.rcl.shared;


/**
 * This class is used to represent the user
 */
public class User {
    private final String name;
    private final String password;
    private Token token;

      public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.token = new Token();
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

}
