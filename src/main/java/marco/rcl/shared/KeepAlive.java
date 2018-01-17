package marco.rcl.shared;

/**
 * Class used to give a standard to the keepalive messages
 */
public class KeepAlive {
    /**
     * encode keepAlive message
     *
     * @param name     username
     * @param password user password
     * @return byte[] containing the message to send to the server
     */
    public static byte[] encodeMessage(String name, String password) {
        return (name + " " + password + " " + System.currentTimeMillis() + " ").getBytes();
    }

    /**
     * decode the keepAlive message
     *
     * @param message message received from the server
     * @return information gathered from the client
     */
    public static String[] decodeMessage(byte[] message) {
        return new String(message).split(" ");
    }
}
