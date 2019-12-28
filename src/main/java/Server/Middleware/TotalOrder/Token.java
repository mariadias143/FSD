package Server.Middleware.TotalOrder;

import java.util.HashSet;
import java.util.Set;

public class Token {
    private int seq_num;
    private Set<String> sequenced;

    public Token(){
        this.seq_num = 1;
        this.sequenced = new HashSet<>();
    }

    public boolean contains(String msg_id){
        return sequenced.contains(msg_id);
    }

    public int getSeqNum(){
        int i = this.seq_num;
        this.seq_num++;
        return i;
    }

    public void addId(String id){
        this.sequenced.add(id);
    }
}
