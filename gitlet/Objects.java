package gitlet;

import java.io.Serializable;

public class Objects implements Serializable {
    private String hash;
    private String content;

    public String getHashHead() {
        return hash.substring(0, 2);
    }

    public String getHashBody() {
        return hash.substring(2);
    }

}
