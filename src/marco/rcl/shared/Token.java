package marco.rcl.shared;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class models the tokens used for authentication between clients/server, it is automatically generated and not
 * editable.
 *
 * @author Marco Barbone
 * @see UUID
 */
public class Token implements Serializable {
    private final static long serialVersionUID = 1L;
    private final UUID id;
    private final long timestamp;
    private final long VALIDITY = TimeUnit.DAYS.toMillis(1);

    public Token() {
        this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @param obj a token
     * @return true if the
     */
    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != Token.class)) return false;
        Token token = (Token) obj;
        return token.id.equals(this.id);
    }

    /**
     * This function is used to check if the token is still valid
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid() {
        return (System.currentTimeMillis() - timestamp) < VALIDITY;
    }
}
