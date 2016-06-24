package marco.rcl.simpleserver;

import marco.rcl.shared.Configs;
import marco.rcl.shared.LoggerFactory;
import marco.rcl.shared.ServerCallbackManager;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {

    private static Logger log = null;
    private static Configs configs = null;
    private static final String directory = "./ServerData";
    private static final String logDirectory = "./logs";

    private static void getConfigs(){
        try {configs = new Configs();} catch (IOException | ParseException e) {
            log.severe("Failed getting configs parameters from file " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        log.info("config correctly read");
    }

    private static void setUp(String directory){
        Path dir = FileSystems.getDefault().getPath(directory);
        try {Files.createDirectory(dir);}
        catch (FileAlreadyExistsException ignored) {}
        catch (IOException e) {
            e.printStackTrace();
            log.severe("failed creation of the directory " + e.toString());
            System.exit(2);
        }
        log.info("directory correctly created");
    }

    public static void main(String[] args) {
        try {
            log = LoggerFactory.getLogger("serverLogger","server");
        } catch (IOException e) {
            System.err.println("failed creation of log file");
            System.exit(-1);
        }
        getConfigs();
        setUp(directory);
        setUp(logDirectory);
        ExecutorService ex = Executors.newCachedThreadPool();
        FriendManager friendManager = new FriendManager(configs, ex);
        UserManager userManager = new UserManager(ex,friendManager);
        KeepAliveManager keepAliveManager = new KeepAliveManager(userManager.getUsers(),configs,ex);
        ConnectionManager connectionManager = new ConnectionManager(configs,ex,userManager);
        CallbackManager callbackManager = new CallbackManager(friendManager,userManager,configs,ex);
        try {
            ServerCallbackManager cm = (ServerCallbackManager) UnicastRemoteObject.exportObject(callbackManager,(int)configs.CallbackPort);
            Registry registry = LocateRegistry.createRegistry((int)configs.CallbackPort);
            registry.rebind(ServerCallbackManager.OBJECT_NAME, cm);
            log.info("Callback manager ready");
        } catch (RemoteException e) {
            e.printStackTrace();
            log.severe("problems with RMI" + e.toString());
        }
        userManager
                .setCallbackManager(callbackManager)
                .setKeepAliveManager(keepAliveManager);
        friendManager.startRemovingExpiredRequests();
        friendManager.startDumpingFriendships();
        callbackManager.startBackup();
        keepAliveManager.startUpdatingStatus();
        connectionManager.startManaging();
        log.info("finished starting");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            System.out.println("write something to exit");
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        friendManager.stopRemovingExpiredRequests();
        friendManager.stopDumpingFriendships();
        callbackManager.stopBackup();
        keepAliveManager.stopUpdatingStatus();
        connectionManager.stopManaging();
        connectionManager.close();
        ex.shutdownNow();
    }



    public static Logger getLog() {
        return log;
    }
}