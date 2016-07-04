package marco.rcl.shared;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Class used to get Logger
 */
public class LoggerFactory {
    // used the factory design pattern here
    public static Logger getLogger(String name, String fileName) throws IOException {
        Logger log = Logger.getLogger(name);
        log.setUseParentHandlers(false);
        FileHandler fh = new FileHandler("./logs/" + fileName + "%u.log");
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new MyFormatter());
        fh.setFormatter(new MyFormatter());
        log.addHandler(fh);
        log.addHandler(ch);
        return log;
    }
}
