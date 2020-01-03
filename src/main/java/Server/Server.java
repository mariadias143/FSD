package Server;

import Client.Request.GetLastTopics;
import Client.Request.RequestsI;
import Client.Request.SignIn;
import Server.Log.LeaderLog;
import Server.Log.Slaves;
import Server.Middleware.Persistency.Persistency;
import Server.ServerLogic.CommunicationQueue;
import Server.ServerLogic.Management;
import io.atomix.utils.net.Address;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    //public TotalOrderFixedSequencer totalorder;
    //public TotalOrderMovingSequencer totalorder;
    public Ordering totalorder;
    public Management man;
    public Persistency persistency;

    public Server(int port,int number_of_peers,boolean moving){
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

        CommunicationQueue<Message<RequestsI>> totalorder_to_persist = new CommunicationQueue<>();
        CommunicationQueue<Message<RequestsI>> persist_to_delivery = new CommunicationQueue<>();


        //this.totalorder = new TotalOrderFixedSequencer(elector,service,queue);
        //this.totalorder = new TotalOrderMovingSequencer(peers,service,idp,this.buildSeqs(2),discoverSequencers(2,number_of_peers),queue);
        int n_seqs = number_of_peers / 2;
        if (moving){
            this.totalorder = new TotalOrderMovingSequencer(
                    peers,
                    service,
                    idp,
                    this.buildSeqs(n_seqs),
                    discoverSequencers(n_seqs,number_of_peers),
                    totalorder_to_persist);
        }
        else {
            this.totalorder = new TotalOrderFixedSequencer(elector,service,totalorder_to_persist);
        }

        this.persistency = new Persistency(
                persist_to_delivery,
                totalorder_to_persist,
                new LeaderLog(String.valueOf(port), this.peers,this.service),
                new Slaves(String.valueOf(port),this.peers,elector,service),
                elector);


        ms.registerHandler("GET",(a,b)->{
            RequestsI r = this.service.s.decode(b);
            r.setServer_id(idp);
            r.setAddress(Address.from(a.port()));
            Message<RequestsI> m = new Message<>(String.valueOf(port)+"-"+1,0,r);
            this.totalorder.send(m);
        },e);

        ms.registerHandler("POST",(a,b)->{
            RequestsI r = this.service.s.decode(b);
            /**
            try {
                r = this.service.s.decode(b);
            }
            catch (Exception ee){
                System.out.println("Error");
                ee.printStackTrace();
            }
            //RequestsI r = this.service.s.decode(b);*/
            System.out.println("Passou no decode");
            r.setServer_id(idp);
            r.setAddress(Address.from(a.port()));
            Message<RequestsI> m = new Message<>("",0,r);
            this.totalorder.send(m);
        },e);

        ms.registerHandler("TESTE",(a,b)->{
            System.out.println(man.toString());
        },e);

        man = new Management(this.service,persist_to_delivery,this.idp);

        new Thread(man).start();
        new Thread(persistency).start();

        if (idp == 0){
            this.elector.init_election();
        }
    }

    public Map<Integer,Boolean> discoverSequencers(int n_seqs,int n_peers){
        Map<Integer,Boolean> res = new HashMap<>();
        for (int i = 0; i < n_peers; i++) {
            if (i < n_seqs){
                res.put(i,true);
            }
            else {
                res.put(i,false);
            }
        }

        return res;
    }

    public Address[] buildSeqs(int n_seqs){
        Address[] res = new Address[n_seqs];
        for (int i = 0; i < n_seqs; i++) {
            res[i] = Address.from(i + 12345);
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        int n_peers = Integer.parseInt(args[1]);
        boolean moving = Boolean.parseBoolean(args[2]);

        Server twitter = new Server(port,n_peers,moving);
    }
}
