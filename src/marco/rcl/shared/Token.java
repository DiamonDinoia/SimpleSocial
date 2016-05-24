package marco.rcl.shared;

import java.util.UUID;

/**
 * Created by Marco on 24/05/16.
 */
public class Token {
    private final UUID id;
    private final long timestamp;

    public Token(long timestamp) {
        this.id = UUID.randomUUID();
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj){
        if ((null == obj) || (obj.getClass() != Token.class))
            return false;
        Token token = (Token) obj;
        return token.id.equals(this.id);
    }
}
