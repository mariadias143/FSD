package Server.ServerLogic;

import Client.Reply.GetLastTopicsReply;
import Client.Reply.Reply;
import Client.Reply.WriteReply;
import Client.Request.*;
import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import Server.Models.Topic;
import Server.Models.Tuple;
import Server.Models.User;

import java.util.*;
import java.util.stream.Collectors;

public class Management implements Runnable {
    private ServerUtil service;
    private CommunicationQueue<Message<Request>> requests;
    private int process_id;
    private Map<String, Topic> topics;
    private Map<String, User> users;


    public Management(ServerUtil service,CommunicationQueue requests,int process_id){
        this.service = service;
        this.requests = requests;
        this.process_id = process_id;
        this.topics = new TreeMap<>();
        this.users = new TreeMap<>();
    }


    public void run () {

        try{
            while (true){
                Message<Request> r = requests.get();

                if (r.getData() instanceof Get){
                    handleReads(r.getData());
                }
                else{
                    handleWrites(r.getData(),r.getTimestamp());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void handleWrites(Request r,int timestamp){
        String object = r.getClass().getSimpleName();

        switch (object){
            case "PostMessage":{

                if (!authentication(r.getUsername(),r.getPassword())){
                    if (r.getServer_id() == this.process_id){
                        Reply reply = new WriteReply(1);
                        reply.reply(this.service.ms,r.getIp(),this.service.s);
                    }
                    return;
                }

                handlePostMessage(r,timestamp);
                break;
            }
            case "SignIn":{
                handleSignIn(r);
                break;
            }
            case "Subscribe":{

                if (!authentication(r.getUsername(),r.getPassword())){
                    if (r.getServer_id() == this.process_id){
                        Reply reply = new WriteReply(1);
                        reply.reply(this.service.ms,r.getIp(),this.service.s);
                    }
                    return;
                }

                handleSubscribe(r);
                break;
            }
            default:
                break;
        }
    }

    private void handleSubscribe(Request r){
        Subscribe subs = (Subscribe) r;

        User u = this.users.get(r.getUsername());
        u.subscribe(subs.getTopics().stream().collect(Collectors.toList()));

        if (r.getServer_id() == this.process_id) {
            Reply reply = new WriteReply();
            reply.reply(this.service.ms, r.getIp(), this.service.s);
        }
    }

    private void handleSignIn(Request r){
        Reply reply;
        if (this.users.containsKey(r.getUsername())){
            if (r.getServer_id() == this.process_id) {
                reply = new  WriteReply(2);
                reply.reply(this.service.ms,r.getIp(),this.service.s);
            }
            return;
        }

        User u = new User(r.getUsername(),r.getPassword());
        this.users.put(r.getUsername(),u);

        if (r.getServer_id() == this.process_id) {
            reply = new WriteReply();
            reply.reply(this.service.ms, r.getIp(), this.service.s);
        }
    }

    private void handlePostMessage(Request r,int timestamp){
        PostMessage pst = (PostMessage) r;

        for(String topic : pst.getTopics()){
            Topic t;
            if (!this.topics.containsKey(topic)){
                t = new Topic(topic);
                this.topics.put(topic,t);
            }
            t = this.topics.get(topic);
            t.post(pst.getMsg(),timestamp);
        }

        if (r.getServer_id() == this.process_id){
            Reply reply = new WriteReply();
            reply.reply(this.service.ms,r.getIp(),this.service.s);
        }
    }


    private void handleReads(Request r){
        String object = r.getClass().getSimpleName();

        if (r.getServer_id() != this.process_id)
            return;

        switch (object){
            case "GetLastTopics":{
                this.handleGetLastTopics((GetLastTopics) r);
                break;
            }
            default:
                break;
        }

    }

    private void handleGetLastTopics(GetLastTopics request){
        GetLastTopicsReply reply;
        if (!authentication(request.getUsername(),request.getPassword())){
           reply = new GetLastTopicsReply(1);
        }
        else {
            User u = this.users.get(request.getUsername());
            List<String> topics = u.getSubscribed();
            List<Tuple<Integer,String>> queryTosort = new ArrayList<>();

            for(String key : topics){
                List<Tuple<Integer,String>> posts = this.topics.get(key).getPosts();

                queryTosort.addAll(posts);
            }

            queryTosort.sort((left,right) -> right.getFirst() - left.getFirst());

            List<String> top10msg = queryTosort.subList(0, queryTosort.size() < 10 ? queryTosort.size() : 10)
                    .stream().map(t -> t.getSecond())
                    .collect(Collectors.toList());

            reply = new GetLastTopicsReply(top10msg);
        }

        reply.reply(this.service.ms,request.getIp(),this.service.s);
    }

    private boolean authentication(String username,String password){
        if (!this.users.containsKey(username)){
            return false;
        }

        User u = users.get(username);

        return u.verifyCredencials(username,password);
    }
}
