package Server.Middleware.TotalOrder;

public class Message<F> implements Comparable<Message<F>> {
    private String id;
    private int timestamp;
    private F data;

    public Message(String id,int timestamp,F data){
        this.id = id;
        this.timestamp = timestamp;
        this.data = data;
    }

    public Message<F> clone(F cloned_data){
        return new Message<>(id,this.timestamp,data);
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public F getData() {
        return data;
    }

    @Override
    public int compareTo(Message<F> o) {
        if (this.timestamp < o.timestamp)
            return -1;
        if (this.timestamp > o.timestamp)
            return 1;
        return 0;
    }
}