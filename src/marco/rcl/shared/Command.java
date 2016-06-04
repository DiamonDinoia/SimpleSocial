package marco.rcl.shared;

/**
 * Support class used to model commands
 */

public class Command {
    private final int command;
    private final String name;
    private final String password;
    private final Token token;
    private final String search;
    private final String address;
    private final int port;

    public Command(String password, String name, int command) {
        this.password = password;
        this.name = name;
        this.command = command;
        this.token = null;
        this.search = null;
        this.address = null;
        this.port = -1;
    }

    public Command(int command, String name, String password, Token token) {
        this.command = command;
        this.name = name;
        this.password = password;
        this.token = token;
        this.search = null;
        this.address = null;
        this.port = -1;
    }

    public Command(int command, String name, String password, Token token, String search) {
        this.command = command;
        this.name = name;
        this.password = password;
        this.token = token;
        this.search = search;
        this.address = null;
        this.port = -1;
    }

    public Command(int command, String name, String password, String address, int port) {
        this.command = command;
        this.name = name;
        this.password = password;
        this.token = null;
        this.search = null;
        this.address = address;
        this.port = port;
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

    public String getSearch() {
        return search;
    }
}
