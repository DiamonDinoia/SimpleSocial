package marco.rcl.simpleClient;

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
 * Created by marko on 06/06/2016.
 */
public class CallbackHandler {

    private ServerCallbackManager callbackManager;
    private ClientCallback callback;
    private static final Logger log = Client.getLog();

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

    public void register(String name, String password, Token token) {
        callback = new ContentCallback();
        try {
            ClientCallback stub = (ClientCallback) UnicastRemoteObject.exportObject(callback,0);
            callbackManager.register(stub,name,password,token);
        } catch (RemoteException e) {
            log.severe("problem registering callback " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Errors follow(String user, String name, String password, Token token){
        try {
            return callbackManager.follow(user, name, password, token);
        } catch (RemoteException e) {
            log.info("something went wrong" + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void close(){
        try {
            UnicastRemoteObject.unexportObject(callback,true);
        } catch (NoSuchObjectException e) {
            log.severe("problem unsporting callback " + e.toString());
        }
    }
}
