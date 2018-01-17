package marco.rcl.simpleClient;

import marco.rcl.shared.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**
 * this class just implement the interface of the Callback
 */
public class ContentCallback extends RemoteObject implements ClientCallback {

    private final static long serialVersionUID = 1L;

    /**
     * this function adds the content to the contents list
     *
     * @param content message from the server
     */
    @Override
    public void content(String content) throws RemoteException {
        SimpleGUI.addMessage(content);
    }
}
