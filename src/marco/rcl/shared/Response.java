package marco.rcl.shared;

/**
 * Created by Marco on 31/05/16.
 */
public class Response {

    private final int error;
    private final Token token;
    private final UserShared[] userList;

    public Response(int error) {
        this.error = error;
        token = null;
        userList = null;
    }

    public Response(Token token) {
        this.error = Errors.noErrors;
        this.token = token;
        userList = null;
    }

    public Response(){
        this.error = Errors.noErrors;
        token = null;
        userList = null;
    }

    public Response(int error, Token token) {
        this.error = error;
        this.token = token;
        userList = null;
    }

    public Response(UserShared[] userList){
        this.userList = userList;
        this.token = null;
        this.error = Errors.noErrors;
    }

    public int getError() {
        return error;
    }


    public Token getToken() {
        return token;
    }

    public UserShared[] getUserList(){return userList;}
}
