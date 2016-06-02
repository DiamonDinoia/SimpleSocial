package marco.rcl.simpleserver;


import marco.rcl.shared.Token;

/**
 * This class is used to represent the user
 */
public class User {
    private final String name;
    private final String password;
    transient private Token token;
    transient private boolean online;


    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.token = new Token();
        this.online = false;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
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

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != User.class)) return false;
        User user = (User) obj;
        return this.name.equals(user.name) && this.password.equals(user.password);
    }

}