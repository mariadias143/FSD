package Server.Models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User {
    private String username;
    private Set<String> subscribed;

    public User(String name){
        this.username = name;
        this.subscribed = new HashSet<>();
    }

    public boolean isSubscribed(String topic){
        return subscribed.contains(topic);
    }

    public void subscribe(List<String> topics){
        topics.forEach(s -> this.subscribed.add(s));
    }

    public void unsubscribe(String topic){
        this.subscribed.remove(topic);
    }
}