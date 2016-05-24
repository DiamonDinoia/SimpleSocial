package marco.rcl.shared;

import java.net.InetAddress;

/**
 * this interface contains all connection parameters
 * TODO: would be nice to remove this inteface and create a config file
 * Created by marko on 23/05/2016.
 */
public interface ConnectionParam {
    int PORT = 36794;
    InetAddress ADDRESS = InetAddress.getLoopbackAddress();
}
