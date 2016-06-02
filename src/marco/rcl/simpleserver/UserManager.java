package marco.rcl.simpleserver;


import marco.rcl.shared.Command;
import marco.rcl.shared.Commands;
import marco.rcl.shared.Errors;
import marco.rcl.shared.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to manage users
 * Created by Marco on 29/05/16.
 */
class UserManager {
    private static Logger log = Server.getLog();
    private ConcurrentHashMap<String,User> users;
    private final static String userFilename = "./ServerData/Users.ser";
    private LinkedBlockingQueue<Socket> connections;
    private ExecutorService ex;
    private boolean manage = false;
    private boolean append = false;

    UserManager() {
        users = DiskManager.restoreFromDisk(userFilename);
        if (users == null) throw new RuntimeException("problems restoring users");
        connections = new LinkedBlockingQueue<>();
        ex = Executors.newCachedThreadPool();
    }

    void submit(Socket socket) throws InterruptedException{
        connections.put(socket);
    }

    public void startManaging(){
        manage=true;
        ex.submit(this::responding);
    }

    private Response register(String name, String password){
        if (name==null) return new Response(Errors.UsernameNotValid,null);
        if (password==null) return new Response(Errors.PasswordNotValid,null);
        if (users.containsKey(name)) return new Response(Errors.UserAlreadyRegistered, null);
        User u = new User(name, password);
        users.put(name,u);
        synchronized (this) {
            DiskManager.updateUserFile(u, userFilename, append);
            append = true;
        }
        return new Response(Errors.noErrors,u.getToken());
    }

    private Response decodeCommand(Command command){
        switch (command.getCommand()){
            case Commands.Register:
                return register(command.getName(),command.getPassword());

            default:return null;
        }
    }

    private void responde(Socket socket){
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Command command = (Command) in.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void responding(){
            try {
                while (manage){
                    Socket socket = connections.take();
                    ex.submit(() -> responde(socket));
                }
            } catch (InterruptedException e) {
                log.info("UserMager interrupted, exiting...");
                e.printStackTrace();
        }

    }

}
