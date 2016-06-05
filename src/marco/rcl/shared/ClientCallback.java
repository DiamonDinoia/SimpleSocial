package marco.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by marko on 05/06/2016.
 */
public interface ClientCallback extends Remote {

    void content(String content) throws RemoteException;

}
