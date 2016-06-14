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

    public static Logger getLog() {
        return log;
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

    /**
     * TODO: check why you get response null
     * @param code
     * @return
     */
    private static Response sendCommand(Commands code){
        if (tcp.sendCommand(new Command(code,name,password)
                .setContent(content)
                .setUser(user)
                .setToken(token)
                .setAddress(address)
                .setPort(port))){
            System.out.println("login error");
            close();
        }
        Response response = tcp.getResponse();
        if (response==null){
            System.out.println("error getting response");
            close();
        }
        Errors.printErrors(response.getError());
        if (response.getError()==Errors.UserNotLogged) {
            while(!login());
            return sendCommand(code);
        }
        return response;
    }

    private static boolean login(){
        System.out.println("type the username");
        name = scanner.next();
        System.out.println("type the password");
        password = scanner.next();
        Response response = sendCommand(Login);
        token = response.getToken();
        if (response.getError()==Errors.UserNotRegistered) while(!register());
        return response.getError()==Errors.noErrors;
    }

    private static boolean register(){
        System.out.println("type the username");
        name = scanner.next();
        System.out.println("type the password");
        password = scanner.next();
        Response response = sendCommand(Register);
        token = response.getToken();
        if (response.getError()==Errors.UserAlreadyRegistered) return login();
        return response.getError()==Errors.noErrors;
    }

    private static void printCommands(){
        System.out.println("type 0  to search user\n"
         + "type 1  to send a friend request\n"
         + "type 2  to confirm a friend request\n"
         + "type 3  to ignore a friend request\n"
         + "type 4  to see your friend list\n"
         + "type 5  to publish something\n"
         + "type 6  to follow one of your friends\n"
         + "type 7  to see your friend request\n"
         + "type 8 to see your followers content\n"
         + "anything else to logout and quit\n");
    }




    public static void main(String[] args) {
        try {
            log =  LoggerFactory.getLogger("clientLogger","client");
        } catch (IOException e) {
            System.err.println("failed creation of logfile");
            System.exit(-1);
        }
        setUp();
        getConfigs();
        int code = -1;
        tcp = new TCPHandler(configs, friendsRequests,ex);
        handler = new CallbackHandler(contents, (int) configs.CallbackPort);
        address = tcp.getAddress();
        port = tcp.getPort();
        tcp.startReceiving();
        System.out.println("Client Started: type 0 to register 1 to login or anything else to exit");
        code = scanner.nextInt();
        log.info("client started");
        if (code==0) while (!register());
        else if (code==1) while (!login());
        else close();
        responder = new KeepAliveResponder(configs,ex,name,password);
        responder.startResponding();
        handler.register(name,password,token);
        while (code>=0 && code<9){
            printCommands();
            user = null;
            content = null;
            switch (scanner.nextInt()){
                case 0:
                    System.out.println("type user name");
                    user = scanner.next();
                    String [] userlist = sendCommand(SearchUser).getUserList();
                    if (userlist==null){
                        System.out.println("user not found");
                        break;
                    }
                    for (String s : userlist) {
                        System.out.println(s);
                    }
                    break;
                case 1:
                    System.out.println("type user name");
                    user = scanner.next();
                    sendCommand(FriendRequest);
                    break;
                case 2:
                    System.out.println("type user name");
                    user = scanner.next();
                    sendCommand(FriendConfirm);
                    friendsRequests.remove(user);
                    break;
                case 3:
                    System.out.println("type user name");
                    user = scanner.next();
                    sendCommand(FriendIgnore);
                    friendsRequests.remove(user);
                    break;
                case 4:
                    for (UserShared shared : sendCommand(FriendList).getFriendList()) {
                        System.out.println(shared.getName() + "Online:" + shared.isOnline());
                    }
                    break;
                case 5:
                    System.out.println("type the content");
                    content = scanner.next();
                    sendCommand(Publish);
                    break;
                case 6:
                    System.out.println("type user name");
                    user = scanner.next();
                    Errors.printErrors(handler.follow(user,name,password,token));
                    break;
                case 7:
                    if (friendsRequests==null){
                        System.out.println("You have no firends... XD");
                        break;
                    }
                    for (String request : friendsRequests) {
                        System.out.println(request);
                    }
                    break;
                case 8:
                    for (String s : contents) {
                        System.out.println(s);
                        contents.remove(s);
                    }
                    break;
            }

        }

    }
}


