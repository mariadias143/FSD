package Client.Request;


import Client.Client;
import Client.Request.Request;

public class Post extends Request {
    private String request;
    public Post(String request){
        this.request = request;
        super.setRequest(this.request);
    }
}
