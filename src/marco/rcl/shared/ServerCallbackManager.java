package marco.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by marko on 05/06/2016.
 */
public interface ServerCallbackManager extends Remote {

    public static final String OBJECT_NAME = "CALLBACK_MANAGER";

    int register(ClientCallback c, String name, String password, Token token) throws RemoteException;
    int follow(String friendName, String name, String password, Token token) throws RemoteException;




}
