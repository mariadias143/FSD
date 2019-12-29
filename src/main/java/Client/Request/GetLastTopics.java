package Client.Request;

public class GetLastTopics extends Get{
    public GetLastTopics(String username,String password){
        super("GET",username,password);
    }
}
