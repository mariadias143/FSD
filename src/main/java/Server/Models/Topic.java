package Server.Models;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.*;

public class Topic {
    private String name;
    private Queue<Tuple<Integer,String>> posts;

    public Topic(String name){
        this.name = name;
        this.posts = new CircularFifoQueue<Tuple<Integer,String>>(10);
    }

    public void post(String post,int timestamp){
        this.posts.add(new Tuple<>(timestamp,post));
    }

    public List<Tuple<Integer,String>> getPosts(){
        List<Tuple<Integer,String>> list = this.posts.stream().collect(Collectors.toList());

        return list;
    }
}
