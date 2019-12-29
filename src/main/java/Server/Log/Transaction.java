package Server.Log;

public class Transaction {
    private int number;
    String request;

    public Transaction(int number,String request){
        this.number=number;
        this.request=request;
    }

    public int getNumber() {
        return number;
    }

    public String getRequest() {
        return request;
    }
}
