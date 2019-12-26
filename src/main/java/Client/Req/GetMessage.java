package Client.Req;



public class GetMessage extends Request {

    private String request;
    public GetMessage(){
        this.request = "GET/messages";
        super.setRequest(this.request);
    }

}
