package marco.rcl.shared;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

/**
 * this class reads the config parameters from a JSON file on the disk
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
    public final long BackupInterval;
    public final String FollowersFileName;
    public final String FollowingFileName;
    public final String PendingContents;
    public final long CallbackPort;

    public Configs() throws IOException, ParseException {
        String fileName = "config.cfg";
        try (FileReader reader = new FileReader(fileName)) {
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
            BackupInterval = (long) json.get("BackupInterval");
            FollowersFileName = (String) json.get("FollowersFileName");
            PendingContents = (String) json.get("PendingContents");
            CallbackPort = (long) json.get("CallbackPort");
            FollowingFileName = (String) json.get("FollowingFileName");
        }
    }

    public Configs(String fileName) throws IOException, ParseException {
        try (FileReader reader = new FileReader(fileName)) {
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
            BackupInterval = (long) json.get("BackupInterval");
            FollowersFileName = (String) json.get("FollowersFileName");
            PendingContents = (String) json.get("PendingContents");
            CallbackPort = (long) json.get("CallbackPort");
            FollowingFileName = (String) json.get("FollowingFileName");
        }
    }
}
