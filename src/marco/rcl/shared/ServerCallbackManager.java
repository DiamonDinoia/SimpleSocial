package marco.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface allows the users to register their callbacks and follow other users
 */
public interface ServerCallbackManager extends Remote {

    String OBJECT_NAME = "CALLBACK_MANAGER";

    /**
     * this function allows user to register their callbacks
     * @param c user callback
     * @param name user name
     * @param password user password
     * @param token user token, to validate the connection
     * @return Error or confirm message
     */
    Errors register(ClientCallback c, String name, String password, Token token) throws RemoteException;

    /**
     * this function is used by the user to follow one of their friend
     * @param friendName friend name
     * @param name user name
     * @param password user password
     * @param token token to validate the connection
     * @return Error or confirm message
     */
    Errors follow(String friendName, String name, String password, Token token) throws RemoteException;




}
