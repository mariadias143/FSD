package Server.Log;

import Server.Middleware.TotalOrder.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Transaction {
    private int number;
    private Message request;
    private ArrayList<String> state;

    public Transaction(int number,Message request){
        this.number=number;
        this.request=request;
        this.state = new ArrayList<String>();
    }

    public synchronized int getNumber() {
        return number;
    }

    public synchronized Message getRequest() {
        return request;
    }

    public synchronized ArrayList getState(){
        return this.state;
    }

    public synchronized boolean rollback(){
        if (this.state.contains("A") && this.state.contains("P"))
            return true;

        return false;
    }

    public synchronized void setState(ArrayList<String> s){
        this.state = new ArrayList<>();
        for(String st: s){
            this.state.add(st);
        }

    }

    public synchronized void addState(String s){
        this.state.add(s);
    }

    public synchronized boolean completed(){
        return this.state.contains("A") || this.state.contains("C");
    }

    public synchronized boolean hasVoted(){
        return this.state.contains("P");
    }

    public synchronized boolean hasAborted(){
        return this.state.contains("A");
    }
}
