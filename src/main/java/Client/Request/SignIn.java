package Client.Request;

public class SignIn extends Post {
    public SignIn(String username,String password){
        super("POST",username,password);
    }
}
