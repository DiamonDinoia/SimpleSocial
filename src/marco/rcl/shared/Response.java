package marco.rcl.shared;

/**
 * Created by Marco on 31/05/16.
 */
public class Response {

    private int error;
    private Token token;

    public Response(int error, Token token) {
        this.error = error;
        this.token = token;
    }

    public int getError() {
        return error;
    }

    public Token getToken() {
        return token;
    }
}
