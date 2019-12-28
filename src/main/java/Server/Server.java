package Server;

import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import Server.Middleware.LeaderElection.Election;
import Server.Middleware.TotalOrder.Message;

import Server.Middleware.Util.*;
import Server.Middleware.TotalOrder.*;



public class Server {
    public Scanner in = new Scanner(System.in);
    public Address[] peers;
    public ManagedMessagingService ms;
    public int port;

    public Server(int port){
        this.port = port;
        peers = new Address[]{
                Address.from(12345),
                Address.from(12346),
                Address.from(12347)
        };
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        ManagedMessagingService ms = new NettyMessagingService("Wannabe twitter",
                Address.from(port),
                new MessagingConfig());

        Serializer s = new SerializerBuilder()
        //        .addType(Message.class)
                .addType(Message.class)
        //        .addType(Clock.class)
                .build();

        Server chat = new Server(port);

        int idp = port - 12345;

        ms.start();

        Election elector = new Election(idp,chat.peers,new ServerUtil(ms,e));

        //TotalOrderFixedSequencer totalorder = new TotalOrderFixedSequencer(elector,new ServerUtil(ms,e));
        TotalOrderMovingSequencer totalorder = new TotalOrderMovingSequencer(elector,new ServerUtil(ms,e),idp,chat.peers);

        /**
        if(idp == 0){
            Scanner asc = new Scanner(System.in);
            asc.nextLine();
            elector.init_election();
        }*/





        ms.registerHandler("msg",(a,b) ->{
            Message m = s.decode(b);
            totalorder.send(m);
        },e);
    }
}
