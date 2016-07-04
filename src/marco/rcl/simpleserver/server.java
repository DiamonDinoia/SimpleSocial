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
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * this is the main class of the server, contains only a main that handles start and exit of the server
 */
public class Server {

    private static Logger log = null;
    private static Configs configs = null;
    private static final String directory = "./ServerData";
    private static final String logDirectory = "./logs";
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private static void getConfigs() {
        try {
            configs = new Configs();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setUp(String directory) {
        Path dir = FileSystems.getDefault().getPath(directory);
        try {
            Files.createDirectory(dir);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        getConfigs();
        setUp(directory);
        setUp(logDirectory);
        try {
            log = LoggerFactory.getLogger("serverLogger", "server");
        } catch (IOException e) {
            System.err.println("failed creation of log file");
            System.exit(-1);
        }
        FriendManager friendManager = new FriendManager(configs);
        UserManager userManager = new UserManager(friendManager);
        KeepAliveManager keepAliveManager = new KeepAliveManager(userManager.getUsers(), configs);
        ConnectionManager connectionManager = new ConnectionManager(configs, userManager);
        CallbackManager callbackManager = new CallbackManager(friendManager, userManager, configs);
        try {
            ServerCallbackManager cm = (ServerCallbackManager) UnicastRemoteObject.exportObject(callbackManager, (int) configs.CallbackPort);
            Registry registry = LocateRegistry.createRegistry((int) configs.CallbackPort);
            registry.rebind(ServerCallbackManager.OBJECT_NAME, cm);
            log.info("Callback manager ready");
        } catch (RemoteException e) {
            log.severe("problems with RMI" + e.toString());
            throw new RuntimeException(e);
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
        configs = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("write something to exit");
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        friendManager.close();
        callbackManager.stopBackup();
        keepAliveManager.close();
        connectionManager.close();
        try {
            UnicastRemoteObject.unexportObject(callbackManager, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }


    public static Logger getLog() {
        return log;
    }
}