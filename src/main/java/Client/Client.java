package Client;


import Client.Presentation.Block;
import Client.Reply.Reply;
import Client.Request.Request;
import Client.Presentation.ClientUI;
import Client.Request.*;


import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;


public class Client {

    private final Address myAddress;
    private final Address forwarderAddress;
    private ServerUtil service;
    private String username;
    private String password;
    private Block block;

    public Client(Integer myAddress, Integer serverAddress, Block block) {

        this.myAddress = Address.from(myAddress);
        this.forwarderAddress = Address.from(serverAddress);
        this.block=block;

        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        ManagedMessagingService ms = new NettyMessagingService("Client",
                Address.from(myAddress),
                new MessagingConfig());

        ms.start();


        this.service= new ServerUtil(ms,e);

        ms.registerHandler("REP",(a,b)->{
            Reply rep = service.s.decode(b);
            rep.printContent();
            block.awake();


        },e);


    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void register (String username, String password) {
        RequestsI request = new SignIn(username,password);
        request.send(service.ms, forwarderAddress, service.s);
        block.setWait();

    }

    public boolean verifyCredentials(){
        return this.username != null && this.password!=null;
    }


    public void getMessages(){
        if(verifyCredentials()){
            RequestsI request = new GetLastTopics(this.username,this.password);
            request.send(service.ms,forwarderAddress,service.s);
            block.setWait();
        }
        else
        System.out.println("Falta fazer SetUp");
    }

    public void postMessage(Set<String> topics, String message) {
        if (verifyCredentials()) {
            RequestsI request = new PostMessage(this.username, this.password, message, topics);
            request.send(service.ms,forwarderAddress,service.s);
            block.setWait();
        }
        else
            System.out.println("Falta fazer SetUp");

    }

    public void postTopics (Set<String> topics) {
        if (verifyCredentials()) {
            RequestsI request = new Subscribe(this.username, this.password, topics);
            request.send(service.ms, forwarderAddress, service.s);
            block.setWait();
        }
        else
            System.out.println("Falta fazer SetUp");

    }


    public void shutdown(){
        this.service.ms.stop();
        this.service.e.shutdown();
    }


    public static void main(String[] args) throws Exception {

        int myport = Integer.parseInt(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        Block block = new Block();
        Client client = new Client(myport, serverPort, block);

        ClientUI clientUI = new ClientUI(client, block);

        Thread t =  new Thread(clientUI);
        t.start();

    }

}

