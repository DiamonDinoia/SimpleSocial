package marco.rcl.simpleserver;


import marco.rcl.shared.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static marco.rcl.shared.Commands.*;
import static marco.rcl.shared.Errors.*;

/**
 * This class is used to manage users
 */
class UserManager {
    private static final Logger log = Server.getLog();
    private ConcurrentHashMap<String, User> users;
    private final static String userFilename = "./ServerData/Users.ser";
    private ExecutorService ex = Server.getExecutorService();
    private FriendManager friendManager;
    private CallbackManager callbackManager;
    private KeepAliveManager keepAliveManager;


    public UserManager(FriendManager friendManager) {
        // try to restore the previous session
        users = DiskManager.restoreFromDisk(userFilename);
        // fatal error if the restore fails with unknown error then exit
        if (users == null) {
            log.severe("problems starting UserManager");
            throw new RuntimeException("problems restoring users");
        }
        this.friendManager = friendManager;
        log.info("userManager correctly started");
    }

    /**
     * this function creates a link between UserManager and Keep-aliveManager in order to notify the new users
     *
     * @param keepAliveManager the keep alive manager
     */
    public UserManager setKeepAliveManager(KeepAliveManager keepAliveManager) {
        this.keepAliveManager = keepAliveManager;
        return this;
    }

    /**
     * create a link between CallbackManager and UserManager
     *
     * @param callbackManager the Callback Manager
     */
    public UserManager setCallbackManager(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
        return this;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    /**
     * function used to perform asynchronous updates to de disk, is synchronized because can be called by more threads
     * at once.
     *
     * @param u the new user to be saved on the disk
     */
    private synchronized void updateUserFile(User u) {
        if (DiskManager.updateUserFile(u, userFilename)) {
            log.severe("failed storing users, aborting");
            throw new RuntimeException("problems in storing user");
        }
    }

    /**
     * if the user is correctly registered and logged in set his callback else returns error
     *
     * @param c        user callback
     * @param name     user name
     * @param password user password
     * @param token    user token
     * @return confirm message or error code
     */
    public Errors setCallback(ClientCallback c, String name, String password, Token token) {
        Errors check = checkUser(name, password, token);
        if (check == noErrors) users.get(name).setCallback(c);
        return check;
    }

    /**
     * if the user is correctly registered and logged in returns his callback
     *
     * @param name user name
     * @return user callback or null in case of error
     */
    public ClientCallback getCallback(String name) {
        if (users.get(name).isOnline()) return users.get(name).getCallback();
        return null;
    }

    /**
     * this function checks the user status and return a confirm code or an error
     *
     * @param name     name of the user
     * @param password password of the user
     * @param token    token of the user
     * @return confirm code or error
     */
    public Errors checkUser(String name, String password, Token token) {
        // username must not be null
        if (name == null) return UsernameNotValid;
        // the user must be registered
        if (!users.containsKey(name)) return UserNotRegistered;
        User u = users.get(name);
        // checking user password
        if (password == null || !u.getPassword().equals(password)) return PasswordNotValid;
        // the user must be online
        if (!u.isOnline()) return UserNotLogged;
        // checking the token
        if (token == null || !u.getToken().isValid() || !u.getToken().equals(token)) return TokenNotValid;
        // no errors then
        return noErrors;
    }

    /**
     * function used to handle registration request from the users
     *
     * @param name     Username
     * @param password user's password
     * @return server response, contains the token if the user is correctly registered, or an error message if something
     * went wrong
     */
    private Response register(String name, String password, String address, int port) {
        // if the username is already taken by other users
        if (users.containsKey(name)) return new Response(UserAlreadyRegistered);
        // if the address or the port are null return an error
        if (address == null || port < 0) return new Response(AddressNotValid);
        // if the username and password are either valid, add the user to the registered user
        User u = new User(name, password, address, port);
        users.put(name, u);
        // perform async update to the userFile on the disk
        ex.submit(() -> updateUserFile(u));
        keepAliveManager.notify(name);
        // add the user to the keep alive checklist
        return new Response(u.getToken());
    }

    /**
     * function used to handle login request from the user
     *
     * @param name     Username
     * @param password user's password
     * @return server response, contains the token if the user is correctly registered, or an error message if something
     * went wrong
     */
    private Response login(String name, String password, String address, int port) {
        // the user must be registered
        if (!users.containsKey(name)) return new Response(UserNotRegistered);
        // if the address or the port are null return an error
        if (address == null || port < 0) return new Response(AddressNotValid);
        // checking user password
        if (!users.get(name).getPassword().equals(password)) return new Response(PasswordNotValid);
        // if the user is registered the update his status
        User u = users.get(name)
                .updateToken()
                .setOnline()
                .setAddress(address)
                .setPort(port);
        // add the user to the keep alive checklist
        keepAliveManager.notify(name);
        return new Response(u.getToken());
    }


    /**
     * function used to handle logout request from the user
     *
     * @param name Username
     * @return server response, contains confirm message if the user is correctly registered, or an error message
     * if something went wrong
     */
    private Response logout(String name) {
        User u = users.get(name);
        u.setOffLine()
            .setAddress(null)
            .setCallback(null)
            .setPort(-1);
        return new Response();
    }

    /**
     * function used to handle search request from the user
     *
     * @return server response, contains an array of users if the user is registered and logged-in, or an error message
     * if something went wrong
     */
    private Response search(String searchUser) {
        ArrayList<String> result = new ArrayList<>();
        users.forEach((key, user) -> {
            if (key.toLowerCase().contains(searchUser.toLowerCase()))
                result.add(user.getName());
        });
        if (result.size() == 0) return new Response();
        return new Response(result.toArray(new String[result.size()]));
    }


    /**
     * function used to get the (friend, status) list of the user "name"
     *
     * @param name the owner of the friendList
     * @return the friend List or null if the user has no friends
     */
    private Response friendList(String name) {
        String[] friends = friendManager.getFriendList(name);
        if (friends == null) return new Response();
        ArrayList<UserShared> tmp = new ArrayList<>();
        for (String friend : friends) {
            tmp.add(new UserShared(friend, users.get(friend).isOnline()));
        }
        return new Response(tmp.toArray(new UserShared[tmp.size()]));
    }

    /**
     * This function tries to contact the receiver, if the connection is successful then send the request else returns
     * an error
     *
     * @param sender   the sender of the friend request
     * @param receiver the receiver og the friend request
     * @return confirm message or error
     */
    private Response addFriendRequest(String sender, String receiver) {
        BufferedWriter writer = null;
        try (Socket socket = new Socket()) {
            User u = users.get(receiver);
            if (u == null) return new Response(UserNotRegistered);
            if (!u.isOnline()) return new Response(UserOffline);
            SocketAddress sa = new InetSocketAddress(u.getAddress(), u.getPort());
            socket.connect(sa, (int) TimeUnit.MINUTES.toMillis(1));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(sender + "\n");
            writer.flush();
            writer.close();
            log.info("request correctly sent");
            return new Response(friendManager.addFriendRequest(receiver, sender));
        } catch (Exception e) {
            e.printStackTrace();
            if (writer != null) try {writer.close();} catch (IOException ignored) {}
            log.severe("failed the connection to the user " + receiver + " " + e.toString());
            return new Response(UserOffline);
        }
    }

    /**
     * this function confirms the friend request if possible or returns an error
     *
     * @param receiver receiver og the request
     * @param sender   sender of the request
     * @return confirm message or error
     */
    private Response confirmRequest(String receiver, String sender) {
        return new Response(friendManager.confirmFriendRequest(receiver, sender));
    }

    /**
     * this function ignores the friend request if possible or returns an error
     *
     * @param receiver receiver og the request
     * @param sender   sender of the request
     * @return confirm message or error
     */
    private Response ignoreRequest(String receiver, String sender) {
        return new Response(friendManager.ignoreFriendRequest(receiver, sender));
    }

    /**
     * this function send the content to callback manager, this method can't fail because all the controls are done
     * before
     *
     * @param name    name of the publisher
     * @param content content shared by the publisher
     * @return confirm message
     */
    private Response publish(String name, String content) {
        if (content == null || content.equals("")) return new Response(ContentNotValid);
        callbackManager.publish(name, content);
        return new Response(noErrors);
    }

    /**
     * this function return the pending list of the user called name
     *
     * @param name owner of the pending request list
     * @return null or pending list
     */
    private Response getPendingRequests(String name) {
        return new Response(friendManager.getPendingRequests(name));
    }

    /**
     * this function is used to decode the command from the user and generate the correct response
     *
     * @param command User's request
     * @return error message if the command is invalid or the confirm message
     */
    public Response decodeCommand(Command command) {
        String name = command.getName();
        String password = command.getPassword();
        Token token = command.getToken();
        log.info("received a command");
        // the name and the password must either be not null
        if (name == null) return new Response(UsernameNotValid);
        if (password == null) return new Response(PasswordNotValid);
        Commands code = command.getCommand();
        // names are not case sensitive
        if (code == Register) return register(name, password, command.getAddress(), command.getPort());
        if (code == Login) return login(name, password, command.getAddress(), command.getPort());
        if (checkUser(name, password, token) != noErrors) return new Response(checkUser(name, password, token));
        if (code == Logout) return logout(name);
        String friend = command.getUser();
        if (code == FriendList) return friendList(name);
        if (code == Publish) return publish(name, command.getContent());
        if (code == PendingRequests) return getPendingRequests(name);
        if (friend == null) return new Response(UserNotValid);
        if (code == SearchUser) return search(friend);
        if (code == FriendRequest) return addFriendRequest(name, friend);
        if (code == FriendConfirm) return confirmRequest(name, friend);
        if (code == FriendIgnore) return ignoreRequest(name, friend);
        return new Response(CommandNotFound);
    }

}
