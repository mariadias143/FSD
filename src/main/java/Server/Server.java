package Server;

import Client.Request.GetLastTopics;
import Client.Request.Request;
import Server.ServerLogic.CommunicationQueue;
import Server.ServerLogic.Management;
import io.atomix.utils.net.Address;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


import Server.Middleware.LeaderElection.Election;
import Server.Middleware.TotalOrder.Message;

import Server.Middleware.Util.*;
import Server.Middleware.TotalOrder.*;



public class Server {
    public Address[] peers;
    public ManagedMessagingService ms;
    public int port;
    public int idp;
    public Election elector;
    public ServerUtil service;
    public TotalOrderFixedSequencer totalorder;

    public Server(int port,int number_of_peers){
        this.port = port;
        this.peers = new Address[number_of_peers];

        for (int i = 0; i < number_of_peers; i++) {
            this.peers[i] = Address.from(12345+i);
        }
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        ManagedMessagingService ms = new NettyMessagingService("Wannabe twitter",
                Address.from(port),
                new MessagingConfig());

        ms.start();

        this.service = new ServerUtil(ms,e);
        this.idp = port - 12345;
        this.elector = new Election(idp,this.peers,service);

        CommunicationQueue<Message> queue = new CommunicationQueue<>();

        this.totalorder = new TotalOrderFixedSequencer(elector,service,queue);
        //TotalOrderMovingSequencer totalorder = new TotalOrderMovingSequencer(elector,new ServerUtil(ms,e),idp,chat.peers);

        ms.registerHandler("msg",(a,b) ->{
            Message m = this.service.s.decode(b);
            totalorder.send(m);
        },e);

        ms.registerHandler("GET",(a,b)->{
            Request r = this.service.s.decode(b);
            r.setServer_id(idp);
            r.setAddress(a);
            Message<Request> m = new Message<>(String.valueOf(port)+"-"+1,0,r);
            this.totalorder.send(m);
        },e);

        ms.registerHandler("POST",(a,b)->{
            Request r = this.service.s.decode(b);
            r.setServer_id(idp);
            r.setAddress(a);
            Message<Request> m = new Message<>(String.valueOf(port)+"-"+1,0,r);
            this.totalorder.send(m);
        },e);

        new Thread(new Management(this.service,queue,this.idp)).start();
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        int n_peers = Integer.parseInt(args[1]);

        Server twitter = new Server(port,n_peers);

        //Election elector = new Election(idp,chat.peers,new ServerUtil(ms,e));

        //TotalOrderFixedSequencer totalorder = new TotalOrderFixedSequencer(elector,new ServerUtil(ms,e));
        //TotalOrderMovingSequencer totalorder = new TotalOrderMovingSequencer(elector,new ServerUtil(ms,e),idp,chat.peers);

        /**
        if(idp == 0){
            Scanner asc = new Scanner(System.in);
            asc.nextLine();
            elector.init_election();
        }*/
    }
}
