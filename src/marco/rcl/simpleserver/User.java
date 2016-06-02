package marco.rcl.simpleserver;


import marco.rcl.shared.Token;

import java.io.Serializable;

/**
 * This class is used to represent the user
 */
public class User implements Serializable{

    private final long serialVersionUID = 1L;

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

    public User setOnline() {
        this.online = true;
        this.token = new Token();
        return this;
    }

    public User setOffLine() {
        this.token = null;
        this.online = false;
        return this;
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

    public User updateToken() {
        this.token = new Token();
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != User.class)) return false;
        User user = (User) obj;
        return this.name.equals(user.name) && this.password.equals(user.password);
    }

}
