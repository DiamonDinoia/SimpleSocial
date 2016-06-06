package marco.rcl.shared;

import java.io.Serializable;

/**
 * Created by Marco on 31/05/16.
 */
public class Response implements Serializable {

    private final static long serialVersionUID = 1L;

    private int error = Errors.noErrors;
    private Token token = null;
    private String[] userList = null;
    private UserShared[] friendList = null;

    public Response(){}

    public Response(int error) {
        this.error = error;
    }

    public Response(Token token) {
        this.token = token;
    }

    public Response(String[] userList){
        this.userList = userList;
    }


    public Response(UserShared[] friendList){
        this.friendList = friendList;
    }

    public int getError() {
        return error;
    }

    public Token getToken() {
        return token;
    }

    public String[] getUserList(){return userList;}

    public UserShared[] getFriendList() {
        return friendList;
    }
}
