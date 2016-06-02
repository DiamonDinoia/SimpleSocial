package marco.rcl.simpleserver;

import marco.rcl.shared.LoggerFactory;
import java.util.logging.Logger;

public class Server {
   private static final Logger log = LoggerFactory.getLogger("serverLogger","server");

    public static void main(String[] args) {
        ConnectionManager cm = new ConnectionManager();
        cm.startManagingConnections();
    }

    public static Logger getLog() {
        return log;
    }
}