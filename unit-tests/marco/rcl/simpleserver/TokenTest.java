package marco.rcl.simpleserver;

import marco.rcl.shared.Token;
import static org.junit.Assert.*;


/**
 * Created by Marco on 25/05/16.
 */
public class TokenTest {

    Token t1,t2;
    @org.junit.Before
    public void initTokens(){
        t1 = new Token();
        t2 = new Token();
    }

    @org.junit.Test
    public void equals() {
        assertTrue(t1.equals(t1));
        assertFalse(t1.equals(t2));

    }


    @org.junit.Test
    public void isValid() {
        assertTrue(t1.isValid());
    }


}