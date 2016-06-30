package marco.rcl.simpleclient;

import marco.rcl.shared.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Vector;

/**
 * this class just implement the interface of the Callback
 */
public class ContentCallback extends RemoteObject implements ClientCallback {

    private final static long serialVersionUID = 1L;
    private Vector<String> contents = null;

    public ContentCallback(Vector<String> content) {
        this.contents = content;
    }

    /**
     * this function adds the content to the contents list
     * @param content message from the server
     */
    @Override
    public void content(String content) throws RemoteException {
        contents.add(content);
        SimpleGUI.addMessage(content);
    }
}
