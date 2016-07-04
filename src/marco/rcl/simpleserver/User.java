package marco.rcl.simpleserver;


import marco.rcl.shared.ClientCallback;
import marco.rcl.shared.Token;

import java.io.Serializable;

/**
 * This class is used to represent the user
 */
public class User implements Serializable {

    private final static long serialVersionUID = 1L;

    private final String name;
    private final String password;
    transient private Token token;
    transient private boolean online;
    transient private String address;
    transient private int port;
    transient private ClientCallback callback;

    public User(String name, String password, String address, int port) {
        this.name = name;
        this.password = password;
        this.token = new Token();
        this.online = false;
        this.address = address;
        this.port = port;
    }

    public ClientCallback getCallback() {
        return callback;
    }

    public User setCallback(ClientCallback callback) {
        this.callback = callback;
        return this;
    }

    public boolean isOnline() {
        return online;
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

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
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

    public User setAddress(String address) {
        this.address = address;
        return this;
    }

    public User setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != User.class)) return false;
        User user = (User) obj;
        return this.name.equals(user.name) && this.password.equals(user.password);
    }

}
