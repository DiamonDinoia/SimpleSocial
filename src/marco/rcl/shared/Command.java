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

    public Command(String password, String name, int command) {
        this.password = password;
        this.name = name;
        this.command = command;
        this.token = null;
        this.search = null;
    }

    public Command(int command, String name, String password, Token token) {
        this.command = command;
        this.name = name;
        this.password = password;
        this.token = token;
        this.search = null;
    }

    public Command(int command, String name, String password, Token token, String search) {
        this.command = command;
        this.name = name;
        this.password = password;
        this.token = token;
        this.search = search;
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
