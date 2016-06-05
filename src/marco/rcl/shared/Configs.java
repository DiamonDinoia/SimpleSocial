package marco.rcl.shared;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * this interface contains all connection parameters
 * Created by marko on 23/05/2016.
 */
public class Configs {
    public final long ServerPort;
    public final String ServerAddress;
    public final String MulticastGroup;
    public final long MulticastPort;
    public final String KeepAliveMessage;
    public final long Backlog;
    public final String DatagramServerAddress;
    public final long DatagramServerPort;
    public final long MaxPacketLength;
    public final String FriendshipFile;
    public final long RequestValidity;

    private  String fileName = "config.cfg";

    public Configs() throws IOException, ParseException {
        FileReader reader = new FileReader(fileName);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(reader);
        ServerPort = (long) json.get("ServerPort");
        ServerAddress = (String) json.get("ServerAddress");
        MulticastGroup = (String) json.get("MulticastGroup");
        MulticastPort = (long) json.get("MulticastPort");
        KeepAliveMessage = (String) json.get("KeepAliveMessage");
        Backlog = (long) json.get("Backlog");
        DatagramServerAddress = (String) json.get("DatagramServerAddress");
        DatagramServerPort = (long) json.get("DatagramServerPort");
        MaxPacketLength = (long) json.get("MaxPacketLength");
        FriendshipFile = (String) json.get("FriendshipFile");
        RequestValidity = (long) json.get("RequestValidity");
        reader.close();
    }

    public Configs(String fileName) throws IOException, ParseException {
        FileReader reader = new FileReader(fileName);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(reader);
        ServerPort = (long) json.get("ServerPort");
        ServerAddress = (String) json.get("ServerAddress");
        MulticastGroup = (String) json.get("MulticastGroup");
        MulticastPort = (long) json.get("MulticastPort");
        KeepAliveMessage = (String) json.get("KeepAliveMessage");
        Backlog = (long) json.get("Backlog");
        DatagramServerAddress = (String) json.get("DatagramServerAddress");
        DatagramServerPort = (long) json.get("DatagramServerPort");
        MaxPacketLength = (long) json.get("MaxPacketLength");
        FriendshipFile = (String) json.get("FriendshipFile");
        RequestValidity = (long) json.get("RequestValidity");
        reader.close();
    }
}
