package marco.rcl.simpleclient;

import marco.rcl.shared.ClientCallback;
import marco.rcl.shared.Errors;
import marco.rcl.shared.ServerCallbackManager;
import marco.rcl.shared.Token;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * This class is used by the client to register the callback to the server and send follow other users
 */
public class CallbackHandler {

    private ServerCallbackManager callbackManager;
    private ClientCallback callback;
    private static final Logger log = Client.getLog();

    /**
     * the constructor simply get the callbackManager from the server
     *
     * @param port public port in which is registered the server callback manager
     */
    public CallbackHandler(int port) {
        try {
            callbackManager = (ServerCallbackManager) LocateRegistry.getRegistry(port)
                    .lookup(ServerCallbackManager.OBJECT_NAME);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            log.severe("problems with rmi callback manager " + e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * This function send the registration command to the server
     *
     * @param name     user name
     * @param password user password
     * @param token    user token
     */
    public void register(String name, String password, Token token) {
        callback = new ContentCallback();
        try {
            ClientCallback stub = (ClientCallback) UnicastRemoteObject.exportObject(callback, 0);
            Errors error = callbackManager.register(stub, name, password, token);
            if (error != Errors.noErrors) {
                log.severe("problem registering callback");
                throw new RuntimeException("problem registering callback");
            }
        } catch (RemoteException e) {
            log.severe("problem registering callback " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * function used by the client to request to follow another user
     *
     * @param user     user to follow
     * @param name     user name
     * @param password user password
     * @param token    user token
     * @return error or confirm code
     */
    public Errors follow(String user, String name, String password, Token token) {
        try {
            return callbackManager.follow(user, name, password, token);
        } catch (RemoteException e) {
            log.info("something went wrong" + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * function used to remove the callback
     */
    public void close() {
        try {
            UnicastRemoteObject.unexportObject(callback, true);
        } catch (NoSuchObjectException e) {
            log.severe("problem unsporting callback " + e.toString());
        }
    }
}
