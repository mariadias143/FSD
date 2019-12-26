package Client.Req;

import java.util.Collection;

public class Subscribe extends Request {
    private String request;
    private Collection<String> topics;

    public Subscribe(Collection<String> topics){
        this.topics = topics;
        this.request="POST/subscribe";
        super.setRequest(this.request);
    }
}
