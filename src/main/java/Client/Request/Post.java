package Client.Request;


import Client.Client;
import Client.Request.Request;

public class Post extends Request {

    public Post(String request,String username,String password){
        super(username,password);
        super.setRequest(request);
    }
}
