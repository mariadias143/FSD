package Client.Request;

import Client.Client;
import Client.Request.Request;

public class Get extends Request {

    public Get(String request,String username,String password){
        super(username,password);
        super.setRequest(request);
    }
}
