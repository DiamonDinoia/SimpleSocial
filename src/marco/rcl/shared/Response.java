package marco.rcl.shared;

import java.io.Serializable;

import static marco.rcl.shared.Errors.noErrors;

/**
 * This class contains all the possible server's responses
 */
public class Response implements Serializable {

    private final static long serialVersionUID = 1L;

    private Errors error = noErrors;
    private Token token = null;
    private String[] userList = null;
    private UserShared[] friendList = null;

    public Response(){}

    public Response(Errors error) {
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

    public Errors getError() {
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
