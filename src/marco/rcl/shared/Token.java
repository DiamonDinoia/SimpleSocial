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
    public boolean equals(Object t){
        Token token = (Token) t;
        return token.id.equals(this.id);
    }
}
