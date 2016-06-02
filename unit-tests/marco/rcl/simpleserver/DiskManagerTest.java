package marco.rcl.simpleserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by marko on 02/06/2016.
 */
public class DiskManagerTest {


    File file = null;
    File file2 = null;
    // update userfile with one user
    @Before
    public void setUpUpdate(){
        file = new File("userfiletest");
    }

    @After
    public void tearDownUpdate(){
        file.delete();
    }

    @Test
    public void updateUserFile() {
        User u = new User("test","test");
        DiskManager.updateUserFile(u,"userfiletest",false);
        ConcurrentHashMap<String,User> testMap = DiskManager.restoreFromDisk("userfiletest");
         assertTrue(testMap.get(u.getName()).equals(u));
    }

    // update userfile with multiple users
    @Before
    public void setUpUpdate2(){
        file2 = new File("userfiletest2");
    }

    @After
    public void tearDownUpdate2(){
        file2.delete();
    }

    @Test
    public void updateUserFile2() {
        User u = new User("test","test");
        User u2 = new User("test2","test2");
        DiskManager.updateUserFile(u,"userfiletest2",false);
        DiskManager.updateUserFile(u2,"userfiletest2",true);
        ConcurrentHashMap<String,User> testMap = DiskManager.restoreFromDisk("userfiletest2");
        assertTrue(testMap.get(u.getName()).equals(u));
        assertTrue(testMap.get(u2.getName()).equals(u2));
    }
    // rstore from disk, corrupted file
    @Before
    public void setUp() {
        file = new File("testfile");

    }

    @After
    public void tearDown() {
        file.delete();

    }

    @Test
    public void restoreFromDisk() {
        assertNotEquals(null,DiskManager.restoreFromDisk("testfile"));
    }

    // restore from disk, file with random data
    @Before
    public void setUp2() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("testfile2"));
            out.writeObject("this test must fail");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown2() {
        file = new File("testfile2");
        file.delete();

    }

    @Test
    public void restoreFromDisk2() {
        assertEquals(null,DiskManager.restoreFromDisk("testfile2"));
    }

    // restore form disk file with correct data
    @Before
    public void setUp3() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("testfile3"));
            out.writeObject(new User("test","test"));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown3() {
        File file = new File("testfile3");
        file.delete();

    }

    @Test
    public void restoreFromDisk3() {
        ConcurrentHashMap<String,User> tmp = DiskManager.restoreFromDisk("testfile3");
        assertNotEquals(null,tmp);
        assertTrue(new User("test","test").equals(tmp.get("test")));
    }



}