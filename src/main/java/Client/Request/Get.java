package Client.Request;

import Client.Client;
import Client.Request.Request;

public class Get extends Request {
    private String request;
    public Get(String request){
        this.request = request;
        super.setRequest(this.request);
    }
}
