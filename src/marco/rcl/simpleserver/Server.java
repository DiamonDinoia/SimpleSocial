package marco.rcl.simpleserver;

import marco.rcl.shared.Configs;
import marco.rcl.shared.LoggerFactory;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.logging.Logger;

public class Server {
   private static final Logger log = LoggerFactory.getLogger("serverLogger","server");

    public static void main(String[] args) {


        Configs configs = null;
        try {
            configs = new Configs();
        } catch (IOException | ParseException e) {
            log.severe("Failed getting configs parameters from file " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }



    }



    public static Logger getLog() {
        return log;
    }
}