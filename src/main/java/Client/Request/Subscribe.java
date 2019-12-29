package Client.Request;

import java.util.Set;

public class Subscribe extends Post {
    private Set<String> topics;

    public Subscribe(String username,String password,Set<String> list_tops){
        super("POST",username,password);
        this.topics = list_tops;
    }

    public Set<String> getTopics() {
        return topics;
    }
}
