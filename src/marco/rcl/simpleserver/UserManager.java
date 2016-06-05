package marco.rcl.simpleserver;


import marco.rcl.shared.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to manage users
 * Created by Marco on 29/05/16.
 */
class UserManager {
    private static final Logger log = Server.getLog();
    private ConcurrentHashMap<String,User> users;
    private final static String userFilename = "./ServerData/Users.ser";
    private LinkedBlockingQueue<Socket> connections;
    private ExecutorService ex;
    private boolean manage = false;
    private boolean append = false;
    private KeepAliveManager keepAliveManager;
    private FriendManager friendManager;

    /**
     * Constructor, creates a new Usermanager
     */
    UserManager(Configs param) {
        // try to restore the previous session
        users = DiskManager.restoreFromDisk(userFilename);
        // fatal error if the restore fails with unknown error then exit
        if (users == null) throw new RuntimeException("problems restoring users");
        connections = new LinkedBlockingQueue<>();
        ex = Executors.newCachedThreadPool();
        // start the keep alive  manager in order to check the status of the users
        keepAliveManager = new KeepAliveManager(users,param,ex);
        friendManager = new FriendManager(param);
    }

    /**
     * @param socket represents che connection with the user
     * @throws InterruptedException
     */
    void submit(Socket socket) throws InterruptedException{
        connections.put(socket);
    }

    /**
     * function used to perform asynchronous updates to de disk, is synchronized because can be called by more threads
     * at once.
     * TODO: use a dispatch list in order to write more object at the time, and check return value
     * @param u the new user to be saved on the disk
     */
    private synchronized void updateUserFile(User u){
        DiskManager.updateUserFile(u, userFilename, append);
        append = true;
    }

    /**
     * this function checks the user status and return a confirm code or an error
     * @param name name of the user
     * @param password password of the user
     * @param token token of the user
     * @return confirm code or error
     */
    private int checkUser(String name,String password, Token token){
        // the user must be registered
        if (!users.containsKey(name)) return Errors.UserNotRegistered;
        User u = users.get(name);
        // checking user password
        if (!u.getPassword().equals(password)) return Errors.PasswordNotValid;
        // the user must be online
        if (!u.isOnline()) return Errors.UserNotLogged;
        // checking the token
        if (token==null || !u.getToken().isValid() || !u.getToken().equals(token)) return Errors.TokenNotValid;
        // no errors then
        return Errors.noErrors;
    }

    /**
     * function used to handle registration request from the users
     * @param name Username
     * @param password user's password
     * @return server response, contains the token if the user is correctly registered, or an error message if something
     * went wrong
     */
    private Response register(String name, String password, String address, int port){
        // if the username is already taken by other users
        if (users.containsKey(name)) return new Response(Errors.UserAlreadyRegistered);
        // if the address or the port are null return an error
        if (address==null || port < 0) return new Response(Errors.AddressNotValid);
        // if the username and password are either valid, add the user to the registered user
        User u = new User(name, password,address,port);
        users.put(name,u);
        // perform async update to the userFile on the disk
        ex.submit(()-> updateUserFile(u));
        // add the user to the keep alive checklist
        return new Response(u.getToken());
    }

    /**
     * function used to handle login request from the user
     * @param name Username
     * @param password user's password
     * @return server response, contains the token if the user is correctly registered, or an error message if something
     * went wrong
     */
    private Response login(String name, String password, String address, int port){
        // the user must be registered
        if (!users.containsKey(name)) return new Response(Errors.UserNotRegistered);
        // if the address or the port are null return an error
        if (address==null || port < 0) return new Response(Errors.AddressNotValid);
        // checking user password
        if (!users.get(name).getPassword().equals(password)) return new Response(Errors.PasswordNotValid);
        // if the user is registered the update his status
        User u = users.get(name)
                    .setOnline()
                    .setAddress(address)
                    .setPort(port);
        // add the user to the keep alive checklist
        return new Response(u.getToken());
    }


    /**
     * function used to handle logout request from the user
     * @param name Username
     * @return server response, contains confirm message if the user is correctly registered, or an error message
     * if something went wrong
     */
    private Response logout(String name) {
        User u = users.get(name);
        u.setOffLine();
        return new Response();
    }

    /**
     * function used to handle search request from the user
     * @return server response, contains an array of users if the user is registered and loggedin, or an error message
     * if something went wrong
     */
    private Response search(String searchUser) {
        ArrayList<String> result = new ArrayList<>();
        users.forEach((key,user)-> {
            if (key.toLowerCase().contains(searchUser.toLowerCase()))
                result.add(user.getName());
        });
        return new Response((String[]) result.toArray());
    }



    /**
     * function used to get the (friend, status) list of the user "name"
     * @param name the owner of the friendList
     * @return the friend List or null if the user has no friends
     */
    private Response friendList(String name){
        String[] friends = friendManager.getFriendList(name);
        if (friends==null) return new Response();
        ArrayList<UserShared> tmp = new ArrayList<>();
        for (String friend : friends) {
            tmp.add(new UserShared(friend,users.get(friend).isOnline()));
        }
        return new Response((UserShared[]) tmp.toArray());
    }

    /**
     * This function tries to contact the receiver, if the connection is successful then send the request else returns
     * an error
     * @param sender the sender of the friend request
     * @param receiver the receiver og the friend request
     * @return confirm message or error
     */
    private Response addFriendRequest(String sender, String receiver){
        try {
            User u = users.get(receiver);
            Socket socket = new Socket();
            SocketAddress sa = new InetSocketAddress(u.getAddress(),u.getPort());
            socket.connect(sa, (int) TimeUnit.MINUTES.toMillis(1));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(sender + "\n");
            writer.flush();
            writer.close();
            socket.close();
            log.info("request correclty sent");
            return new Response(friendManager.addFriendRequest(receiver,sender));
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("failde connecction to the user " + receiver);
            return new Response(Errors.UserOffline);
        }
    }

    /**
     * this function confirms the friend request if possible or returns an error
     * @param receiver receiver og the request
     * @param sender sender of the request
     * @return confirm message or error
     */
    private Response confirmRequest(String receiver, String sender){
        return new Response(friendManager.confirmFriendRequest(receiver,sender));
    }
    /**
     * this function ignores the friend request if possible or returns an error
     * @param receiver receiver og the request
     * @param sender sender of the request
     * @return confirm message or error
     */
    private Response ignoreRequest(String receiver, String sender){
        return new Response(friendManager.ignoreFriendRequest(receiver,sender));
    }


    /**
     * this function is used to decode the command from the user and generate the correct response
     * @param command User's request
     * @return error message if the command is invalid or the confirm message
     */
    private Response decodeCommand(Command command){
        String name = command.getName();
        String password = command.getPassword();
        Token token = command.getToken();
        // the name and the password must either be not null
        if (name==null) return new Response(Errors.UsernameNotValid);
        if (password==null) return new Response(Errors.PasswordNotValid);
        int code = command.getCommand();
        // names are not case sensitive
        name = name.toUpperCase();
        if (code == Commands.Register) return register(name,password,command.getAddress(),command.getPort());
        if (code == Commands.Login) return login(name,password,command.getAddress(),command.getPort());
        if (checkUser(name, password, token) != Errors.noErrors) return new Response(checkUser(name,password,token));
        if (code == Commands.Logout) return logout(name);
        String friend = command.getUser();
        if (friend==null) return new Response(Errors.UserNotValid);
        if (code == Commands.SearchUser) return search(name);
        if (code == Commands.FriendList) return friendList(name);
        // sono qui
        return new Response(Errors.CommandNotFound);
    }

    /**
     * this function is used to perform async communications with the users
     * @param socket the connection with the user
     */
    private void responder(Socket socket){
        // it is all in one try block because is not important which one fails, the user can always try another time
        Command command = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            command = (Command) in.readObject();
            Response response = decodeCommand(command);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            log.info("user " + command.getName() + "correctly handled");
            out.writeObject(response);
        // simply logs the error, this is not a fatal one
        } catch (IOException | ClassNotFoundException e) {
            log.severe("user " + (command!=null ? command.getName() : "" ) + "not correctly handled " + e.toString() );
            e.printStackTrace();
        // cleanup and return
        } finally {
            try {socket.close();} catch (IOException ignored) {}
        }
    }

    /**
     * tells the UserManager to start working, and performing async dispatching
     */
    void startManaging() {
        if (manage) return;
        manage=true;
        ex.submit(() -> {
            try {
                // while not interrupted
                while (manage){
                    Socket socket = connections.take();
                    ex.submit(() -> responder(socket));
                }
                // else terminate
            } catch (InterruptedException e) {
                log.info("UserManager interrupted, exiting...");
                e.printStackTrace();
            }
        });
        keepAliveManager.startUpdatingStatus();
    }
}
