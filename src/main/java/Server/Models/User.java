package Server.Models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private String username;
    private String password;
    private Set<String> subscribed;

    public User(String name,String password){
        this.username = name;
        this.password = password;
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

    public boolean verifyCredencials(String username,String password){
        return this.username.equals(username) && this.password.equals(password);
    }

    public List<String> getSubscribed(){
        return this.subscribed.stream().collect(Collectors.toList());
    }
}