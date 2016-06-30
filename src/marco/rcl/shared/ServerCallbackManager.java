package marco.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by marko on 05/06/2016.
 */
public interface ServerCallbackManager extends Remote {

    String OBJECT_NAME = "CALLBACK_MANAGER";

    Errors register(ClientCallback c, String name, String password, Token token) throws RemoteException;

    Errors follow(String friendName, String name, String password, Token token) throws RemoteException;




}
