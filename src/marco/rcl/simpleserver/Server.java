package marco.rcl.simpleserver;

import marco.rcl.shared.LoggerFactory;

import java.util.logging.Logger;

public class Server {

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("serverLogger","server");
        ConnectionManager cm = new ConnectionManager(log);
        cm.startManagingConnections();
    }
}