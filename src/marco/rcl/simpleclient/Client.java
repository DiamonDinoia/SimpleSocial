package marco.rcl.simpleclient;

import marco.rcl.shared.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static marco.rcl.shared.Commands.*;
import static marco.rcl.shared.Errors.noErrors;

/**
 * This is the Main class of the client.
 */
public class Client {

    private static Logger log = null;
    private static Configs configs = null;
    private static final String directory = "./ClientData";
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ReentrantLock lock = new ReentrantLock();

    private static String name, password;
    private static TCPResponder tcp = null;
    private static KeepAliveResponder responder = null;
    private static CallbackHandler handler;
    private static Token token = null;
    private static String address = null;
    private static int port = -1;
    private static String user = null;
    private static String content = null;


    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Logger getLog() {
        return log;
    }

    /**
     * this function reads the config parameters from the disk
     */
    private static void getConfigs() {
        try {
            configs = new Configs();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * this function creates the directories
     */
    private static void setUp() {
        Path dir = FileSystems.getDefault().getPath(directory);
        try {
            Files.createDirectory(dir);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * this function close the client cleaning all the data structures
     */
    public static void close() {
        if (tcp != null) tcp.close();
        if (handler != null) handler.close();
        if (responder != null) responder.close();
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        System.exit(0);
    }

    /**
     * this function sets and sends the command to the server
     *
     * @param code command code
     * @return server response
     */
    private static Response sendCommand(Commands code) {
        return tcp.sendCommandAndGetResponse(new Command(code, name, password)
                .setContent(content)
                .setUser(user)
                .setToken(token)
                .setAddress(address)
                .setPort(port));
    }

    public static void logout() {
        lock.lock();
        sendCommand(Logout);
        Client.name = null;
        Client.password = null;
        Client.token = null;
        Client.user = null;
        Client.content = null;
        responder.stopResponding();
        handler.close();
        lock.unlock();
    }

    public static Errors login(String name, String password) {
        lock.lock();
        Client.name = name;
        Client.password = password;
        Response response = sendCommand(Login);
        log.info("login command sent");
        token = response.getToken();
        if (response.getError() == noErrors) {
            handler.register(name, password, token);
            responder.startResponding(name, password);
        }
        lock.unlock();
        return response.getError();
    }

    public static Errors register(String name, String password) {
        lock.lock();
        Client.name = name;
        Client.password = password;
        Response response = sendCommand(Register);
        log.info("register command sent");
        token = response.getToken();
        Client.login(name, password);
        lock.unlock();
        return response.getError();
    }

    public static Response searchUser(String user) {
        lock.lock();
        Client.user = user;
        Response response = sendCommand(SearchUser);
        Client.user = null;
        lock.unlock();
        return response;
    }

    public static Errors addFriend(String user) {
        lock.lock();
        Client.user = user;
        Response response = sendCommand(FriendRequest);
        Client.user = null;
        lock.unlock();
        return response.getError();
    }

    public static Response friendList() {
        lock.lock();
        Response response = sendCommand(FriendList);
        lock.unlock();
        return response;
    }

    public static Errors followFriend(String user) {
        lock.lock();
        Errors error = handler.follow(user, name, password, token);
        lock.unlock();
        return error;
    }

    public static Errors publish(String content) {
        lock.lock();
        Client.content = content;
        Response response = sendCommand(Publish);
        Client.content = null;
        lock.unlock();
        return response.getError();
    }

    public static Response friendRequests() {
        lock.lock();
        Response response = sendCommand(PendingRequests);
        lock.unlock();
        return response;
    }

    public static void confirmRequest(String user) {
        lock.lock();
        Client.user = user;
        sendCommand(FriendConfirm);
        Client.user = null;
        lock.unlock();
    }

    public static void ignoreRequest(String user) {
        lock.lock();
        Client.user = user;
        sendCommand(FriendIgnore);
        Client.user = null;
        lock.unlock();
    }

    public static void main(String[] args) {
        setUp();
        getConfigs();
        try {
            log = LoggerFactory.getLogger("clientLogger", "client");
        } catch (IOException e) {
            System.err.println("failed creation of logfile");
            System.exit(-1);
        }
        tcp = new TCPResponder(configs);
        address = tcp.getAddress();
        port = tcp.getPort();
        handler = new CallbackHandler((int) configs.CallbackPort);
        responder = new KeepAliveResponder(configs);
        configs = null;
        tcp.startReceiving();
        log.info("client started");
        SimpleGUI.startView();
    }
}


