package marco.rcl.shared;

/**
 * Created by marko on 03/06/2016.
 */
public class KeepAlive {

    public static byte[] encodeMessage(String name,String password){
        return  (name + " " + password + " " + System.currentTimeMillis() + " ").getBytes();
    }

    public static String[] decodeMessage(byte[] message){
        return new String(message).split(" ");
    }
}
