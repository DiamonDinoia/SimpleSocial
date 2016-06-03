package marco.rcl.simpleserver;

import marco.rcl.shared.ConnectionParam;
import marco.rcl.shared.KeepAlive;
import marco.rcl.shared.LoggerFactory;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.logging.Logger;

public class Server {
   private static final Logger log = LoggerFactory.getLogger("serverLogger","server");

    public static void main(String[] args) {

        ConnectionParam connectionParam=null;
        try {
            connectionParam = new ConnectionParam();
        } catch (IOException | ParseException e) {
            log.severe("Failed getting config parameters from file " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        ConnectionManager cm = new ConnectionManager(connectionParam);
        cm.startManagingConnections();
    }

    public static Logger getLog() {
        return log;
    }
}