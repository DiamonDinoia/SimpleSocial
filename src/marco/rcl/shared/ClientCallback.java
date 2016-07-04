package marco.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface used by the server to publish content to the users
 */
public interface ClientCallback extends Remote {
    /**
     * @param content the content shared by the user that you are following
     * @throws RemoteException
     */
    void content(String content) throws RemoteException;

}
