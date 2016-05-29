package marco.rcl.shared;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Created by marko on 24/05/2016.
 */
public class LoggerFactory {

    public static Logger getLogger(String name, String fileName) {
        try {
            Logger log = Logger.getLogger(name);
            log.setUseParentHandlers(false);
            FileHandler fh = new FileHandler("./logs/"+fileName + "%u.log");
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new MyFormatter());
            fh.setFormatter(new MyFormatter());
            log.addHandler(fh);
            log.addHandler(ch);
            return log;
        } catch (IOException e) {
            System.err.println("failed creation of logfile");
            e.printStackTrace();
            return null;
        }
    }
}