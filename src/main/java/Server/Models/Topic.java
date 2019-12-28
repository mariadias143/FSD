package Server.Models;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.*;

public class Topic {
    private String name;
    private Queue<String> posts;

    public Topic(String name){
        this.name = name;
        this.posts = new CircularFifoQueue<String>(10);
    }

    public void post(String post){
        this.posts.add(post);
    }

    public List<String> getPosts(){
        List<String> list = this.posts.stream().collect(Collectors.toList());

        return list;
    }
}
