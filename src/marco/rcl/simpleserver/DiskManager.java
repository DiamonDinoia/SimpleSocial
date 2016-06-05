package marco.rcl.simpleserver;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * this is a class of static methods that simply save and restore the server's status on the disk
 */
public class DiskManager {

    private static Logger log = Server.getLog();

    /**
     * restores the hashMap from the disk
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
            while (true){
                user = (User) in.readObject();
                registeredUsers.put(user.getName(),user);
            }
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


    /**
     * this function store user's data on the disk in order to prevent data loss in case of crash or exit
     * @param u user to store
     * @param userFilename file in which store
     * @param append boolean says if append at the end or overwrite the file
     * @return true in case of error false otherwise
     */
    public static boolean updateUserFile(User u, String userFilename, boolean append ){
        FileOutputStream fo = null;
        AppendingObjectOutputStream outAppend = null;
        ObjectOutputStream out = null;
        boolean error = false;
        log.info("updating UserFile");
        // try to update
        try {
            fo = new FileOutputStream(userFilename,append);
            if(append){
                outAppend = new AppendingObjectOutputStream(fo);
                outAppend.writeObject(u);
            }else {
                out = new ObjectOutputStream(fo);
                out.writeObject(u);
            }
            log.info("update terminated");
        // if there is an error, modifies can't be saved, terminate and start a new session
        } catch (FileNotFoundException e) {
            log.severe("userFile not found terminating " + e.toString());
            e.printStackTrace();
            error = true;
        } catch (IOException e) {
            log.severe("error during userFile update " + e.toString());
            e.printStackTrace();
            error = true;
        } finally {
            // cleanup and exiting
            try {
                if (fo != null) fo.close();
                if (outAppend != null) outAppend.close();
                if (out!=null) out.close();
            } catch (IOException ignored) {}
        }
        return error;
    }

    /**
     * Function used to save the friend list on the disk, to prevent data loss
     * @param fileName file in which store the friend list
     * @return null if something went wrong the hashMap if everything OK
     */
    public static ConcurrentHashMap<String,ArrayList<String>> RestoreFriendList(String fileName){
        FileInputStream fi = null;
        ObjectInputStream in = null;
        ConcurrentHashMap<String,ArrayList<String>> friends = null;
        log.info("start restoring friend lists");
        try {
            fi = new FileInputStream(fileName);
            in = new ObjectInputStream(fi);
            Object check  = in.readObject();
            if (check instanceof ConcurrentHashMap ){
                friends = (ConcurrentHashMap<String,ArrayList<String>>) check;
            }
            log.info("friend list correctly restored");
        } catch (FileNotFoundException e) {
            log.info("Friend file list not exists creating a new one");
            File f = new File(fileName);
            friends = new ConcurrentHashMap<>();
            try {
                if (f.createNewFile()) log.info("file correctly created");
                    else throw new IOException();
            } catch (IOException e1) {
                log.severe("Impossible create friend file " + e1.toString() );
                e.printStackTrace();
                friends = null;
            }
        } catch (IOException e) {
            log.severe("Error during restore of friend list " + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log.severe("Strange error during restoring friend list " + e.toString());
            e.printStackTrace();
        } catch (ClassCastException e) {
            log.severe("friend file damaged or invalid " + e.toString());
            e.printStackTrace();
        } finally {
            try {
                // cleaning up and exiting
                if (fi != null) fi.close();
                if (in != null) in.close();
            } catch (IOException ignored) {ignored.getLocalizedMessage();}
        }
        return friends;
    }

    /**
     * this function dumps the friend list on the disk
     * @param friendList friend list to dump
     * @param fileName file in which dump the friend list
     * @return true in case of error false otherwise
     */
    public static boolean dumpFriendList(ConcurrentHashMap<String,ArrayList<String>> friendList, String fileName ){
        FileOutputStream fo = null;
        ObjectOutputStream out = null;
        log.info("dump started");
        boolean error = false;
        try {
            fo = new FileOutputStream(fileName);
            out = new ObjectOutputStream(fo);
            out.writeObject(friendList);
            log.info("dump correctly terminated");
        } catch (FileNotFoundException e) {
            log.severe("friend list dump file not exists " + e.toString());
            e.printStackTrace();
            error = true;
        } catch (IOException e) {
            log.severe("error in dump friendList " + e.toString());
            e.printStackTrace();
            error = true;
        } finally {
            // cleaning up and returning
            try {
                if (out != null) out.close();
                if (fo != null) fo.close();
                // instruction added only to silence code duplicate warning
            } catch (IOException ignored) {ignored.toString();}
        }
        return error;
    }
}

/**
 * when java serialize an object on a file writes an header, that makes impossible append an object at the end of a file
 * overriding the writeStreamHeader and forcing it to do nothing you can append an object to the end of the file
 */
class AppendingObjectOutputStream extends ObjectOutputStream {

    public AppendingObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }

}