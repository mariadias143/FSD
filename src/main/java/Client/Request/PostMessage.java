package Client.Request;

import java.util.Set;

public class PostMessage extends Post {
    private String msg;
    private Set<String> topics;

    public PostMessage(String username,String password,String msg,Set<String> list_tops){
        super("POST",username,password);
        this.msg = msg;
        this.topics = list_tops;
    }

    public Set<String> getTopics(){
        return this.topics;
    }

    public String getMsg(){
        return this.msg;
    }
}
