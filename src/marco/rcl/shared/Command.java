package marco.rcl.shared;

import marco.rcl.simpleserver.User;

/**
 * Support class used to model commands
 */

public class Command {
    public Command(int command, String name, String password) {
        this.command = command;
        this.name = name;
        this.password = password;
    }

    private int command;
    private String name;
    private String password;

    public int getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
