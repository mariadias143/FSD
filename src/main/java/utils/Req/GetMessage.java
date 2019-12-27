package utils.Req;

import Client.Request.Request;

public class GetMessage extends Request {

    private String request;
    public GetMessage(){
        this.request = "GET/messages";
        super.setRequest(this.request);
    }

}
