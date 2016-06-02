package marco.rcl.simpleserver;

import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by marko on 01/06/2016.
 */
public class DiskManager {

    private static Logger log = Server.getLog();

    /**
     *
     * @param userFilename name and path of the file containing all the registered users
     * @return ConcurrentHashMap if the function correctly restores the users from the file, null if it fails
     */
    public static ConcurrentHashMap<String,User> restoreFromDisk(String userFilename) {
        ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
        ObjectInputStream in=null;
        FileInputStream fi=null;
        String last_name = null;
        boolean recover = false;
        try {
            // try to read the entire file
            fi = new FileInputStream(userFilename);
            in = new ObjectInputStream(fi);
            User user;
            // this is no the best way to read the entire file, the most correct way is to save on the file some
            // metadata (such as the last saved user or the number of the users) in order to check if the file
            // has been truncated by some error
            do {
                user = (User) in.readObject();
                registeredUsers.put(user.getName(), new User(user.getName(), user.getPassword()));
            } while (true);
        } catch (EOFException e ){
            // reached EOF, checking if it is an error
            // at least I check if the file is empty is an error
            if (registeredUsers.size()>0) log.info("reached EOF finished restoring users");
            else {
                log.severe("error from restoring registered users file damaged... try to recovering  " + e.toString());
                recover = true;
            }
        // if the file not exists is not an error, it means there are no registerd users
        } catch (FileNotFoundException e){
            log.info("Registered users file not exists, no user registered");
        // this is sure an error
        } catch (IOException | ClassNotFoundException | ClassCastException e ) {
            log.severe("error from restoring registered user list from disk " + e.toString());
            e.printStackTrace();
            registeredUsers = null;
            // closing everything before returning
        } finally {
            if (in != null) try {in.close();} catch (IOException ignored) {}
            if (fi != null) try {fi.close();} catch (IOException ignored) {}
            if (recover && registeredUsers.size()<=0) {
                try {
                    Files.delete(FileSystems.getDefault().getPath(userFilename));
                    log.severe("successfully recovered");
                } catch (IOException e) {
                    e.printStackTrace();
                    registeredUsers = null;
                    log.severe("impossible to recover");
                }
            }
        }
        return registeredUsers;
    }



    public static void updateUserFile(User u, String userFilename, boolean append ){
        FileOutputStream fo = null;
        AppendingObjectOutputStream outAppend = null;
        ObjectOutputStream out = null;
        try {
            fo = new FileOutputStream(userFilename,append);
            if(append){
                outAppend = new AppendingObjectOutputStream(fo);
                outAppend.writeObject(u);
            }else {
                out = new ObjectOutputStream(fo);
                out.writeObject(u);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fo != null) fo.close();
                if (outAppend != null) outAppend.close();
                if (out!=null) out.close();
            } catch (IOException e) {}
        }
    }

}


class AppendingObjectOutputStream extends ObjectOutputStream {

    public AppendingObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }

}