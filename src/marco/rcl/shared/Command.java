package marco.rcl.shared;

/**
 * Support class used to model commands
 */

public class Command {
    private int command;
    private String name;
    private String password;
    private Token token;
    private String user;
    private String address;
    private int port;

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
