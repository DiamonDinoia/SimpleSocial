package marco.rcl.shared;

import java.io.Serializable;

/**
 * Support class used to model commands
 */

public class Command implements Serializable {

    private final static long serialVersionUID = 1L;

    private int command;
    private String name;
    private String password;
    private Token token = null;
    private String user = null;
    private String address = null;
    private String content = null;
    private int port = -1;


    public Command(int command, String name, String password) {
        this.command = command;
        this.name = name;
        this.password = password;
    }

    public Command setToken(Token token) {
        this.token = token;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Command setContent(String content) {
        this.content = content;
        return this;
    }

    public Command setUser(String user) {
        this.user = user;
        return this;
    }

    public Command setAddress(String address) {
        this.address = address;
        return this;
    }

    public Command setPort(int port) {
        this.port = port;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getCommand() {
        return command;
    }

    public Token getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }
}
