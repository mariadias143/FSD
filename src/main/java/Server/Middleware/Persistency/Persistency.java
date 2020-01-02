package Server.Middleware.Persistency;

import Client.Presentation.Block;
import Client.Request.RequestsI;
import Server.Log.LeaderLog;
import Server.Log.Slaves;
import Server.Log.Transaction;
import Server.Middleware.LeaderElection.Election;
import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import Server.ServerLogic.CommunicationQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Persistency implements Runnable {
    private ServerUtil service;
    private CommunicationQueue<Message<RequestsI>> delivery;
    private CommunicationQueue<Message<RequestsI>> messaging_queue;
    private LeaderLog leader_log;
    private Slaves slave_log;
    private Election network;
    private Block sync;


    public Persistency(CommunicationQueue<Message<RequestsI>> d1,CommunicationQueue<Message<RequestsI>> s1,LeaderLog l,Slaves s,Election network){
        this.delivery = d1;
        this.messaging_queue = s1;
        this.leader_log = l;
        this.slave_log = s;
        this.network = network;
        this.sync = new Block();
        this.slave_log.setBlock(this.sync);
    }

    @Override
    public void run() {

        try{

            if(this.slave_log.hasCrashed()){
                network.init_election();
                network.waitleader();

                List<Transaction> old_transactions = this.slave_log.transactions_started();
                if (old_transactions.size() == 0)
                    System.out.println("Error a reiniciar");

                List<Message<RequestsI>> to_deliver = old_transactions.subList(0,old_transactions.size()-1)
                        .stream()
                        .filter(transaction -> !transaction.hasAborted())
                        .map(trans->(Message<RequestsI>)trans.getRequest().getData())
                        .collect(Collectors.toList());

                to_deliver.forEach(a -> this.delivery.add(a));
                Transaction to_handle = old_transactions.get(old_transactions.size()-1);
                if (to_handle.completed() == false){
                    if (!to_handle.hasVoted()){
                        this.slave_log.abortTransaction(to_handle);
                    }
                    sync.setWait();
                }
            }



            while (true){

                Message<RequestsI> req = messaging_queue.get();
                System.out.println("Persitencia: inicio" );
                boolean commited = true;
                RequestsI r = req.getData();

                if (r.findType().equals("POST")){
                    Transaction t = new Transaction(req.getTimestamp(),req);

                    this.slave_log.startTransaction(t);
                    System.out.println();
                    if (network.isLeader()){
                        leader_log.openTransaction(t);
                    }
                    sync.setWait();

                    if (t.rollback()){
                        commited = false;
                    }
                }
                if (commited)
                    this.delivery.add(req);
                System.out.println("Persitencia: fim" );
            }
        }
        catch (Exception ee){
            ee.printStackTrace();
        }
    }
}
