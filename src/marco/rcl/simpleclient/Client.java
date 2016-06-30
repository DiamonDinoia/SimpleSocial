package marco.rcl.simpleclient;

import marco.rcl.shared.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static marco.rcl.shared.Commands.*;
import static marco.rcl.shared.Errors.*;

public class Client {

    private static Logger log = null;
    private static Configs configs = null;
    private static final String directory = "./ClientData";
    private static Vector<String> friendsRequests = new Vector<>();
    private static ExecutorService ex = Executors.newCachedThreadPool();
    private static Vector<String> contents = new Vector<>();


    private static Scanner scanner = new Scanner(System.in);
    private static String name, password;
    private static TCPHandler tcp = null;
    private static KeepAliveResponder responder = null;
    private static CallbackHandler handler;
    private static Token token = null;
    private static String address = null;
    private static int port = -1;
    private static String user = null;
    private static String content = null;

    public static ExecutorService getExecutorService() {
        return ex;
    }
    public static Logger getLog() {
        return log;
    }

    public static void submit(Runnable r){
        ex.submit(r);
    }

    private static void getConfigs(){
        try {configs = new Configs();} catch (IOException | ParseException e) {
            e.printStackTrace();
            log.severe("Failed getting configs parameters from file " + e.toString());
            System.exit(1);
        }
        log.info("config correctly read");
    }


    private static void setUp(){
        Path dir = FileSystems.getDefault().getPath(directory);
        try {
            Files.createDirectory(dir);}
        catch (FileAlreadyExistsException ignored) {}
        catch (IOException e) {
            log.severe("failed creation of the directory " + e.toString());
            e.printStackTrace();
            System.exit(2);
        }
        log.info("directory correctly created");
    }

    private static void close(){
        if (tcp != null) {
            tcp.stopReceiving();
            tcp.close();
        }
        System.exit(0);
    }

    private static Response sendCommand(Commands code){
        return tcp.sendCommandAndGetResponse(new Command(code,name,password)
                .setContent(content)
                .setUser(user)
                .setToken(token)
                .setAddress(address)
                .setPort(port));
    }
    public static void logout(){
        sendCommand(Logout);
        Client.name = null;
        Client.password = null;
        Client.token = null;
        responder.stopResponding();
        handler.close();
        handler = null;
    }

    public static Errors login(String name, String password){
        Client.name = name;
        Client.password = password;
        Response response = sendCommand(Login);
        log.info("login command sent");
        token = response.getToken();
        if (response.getError()==noErrors){
            handler = new CallbackHandler(contents, (int) configs.CallbackPort);
            handler.register(name,password,token);
            responder.startResponding(name,password);
        }
        return response.getError();
    }

    public static Errors register(String name, String password){
        Client.name = name;
        Client.password = password;
        Response response = sendCommand(Register);
        log.info("register command sent");
        token = response.getToken();
        if (response.getError()==noErrors){
            handler.register(name,password,token);
            responder.startResponding(name,password);
        }
        return response.getError();
    }

    public static Response searchUser(String user){
        Client.user = user;
        Response response = sendCommand(SearchUser);
        Client.user=null;
        return response;
    }

    public static Errors addFriend(String user){
        Client.user = user;
        Response response = sendCommand(FriendRequest);
        Client.user= null;
        return response.getError();
    }

    public static Response friendList(){
        return sendCommand(FriendList);
    }

    public static Errors followFriend(String user){
        return handler.follow(user,name,password,token);
    }

    public static Errors publish(String content){
        Client.content = content;
        Response response = sendCommand(Publish);
        Client.content = null;
        return response.getError();
    }

    public static Response friendRequests() {
        return sendCommand(PendingRequests);
    }

    public static void confimRequest(String user){
        Client.user = user;
        sendCommand(FriendConfirm);
        Client.user = null;
    }
    public static void ignoreRequest(String user){
        Client.user = user;
        sendCommand(FriendIgnore);
        Client.user = null;
    }

    public static void main(String[] args) {
        try {log =  LoggerFactory.getLogger("clientLogger","client");
        } catch (IOException e) {
            System.err.println("failed creation of logfile");
            System.exit(-1);
        }
        setUp();
        getConfigs();
        int code = -1;
        tcp = new TCPHandler(configs, friendsRequests,ex);
        address = tcp.getAddress();
        port = tcp.getPort();
        responder = new KeepAliveResponder(configs);
        tcp.startReceiving();
        log.info("client started");
        SimpleGUI.startView();
    }
}


