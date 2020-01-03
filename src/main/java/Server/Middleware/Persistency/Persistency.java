package Server.Middleware.Persistency;

import Client.Presentation.Block;
import Client.Request.RequestsI;
import Server.Log.LeaderLog;
import Server.Log.Slaves;
import Server.Log.Transaction;
import Server.Middleware.LeaderElection.Election;
import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import Server.Models.Tuple;
import Server.ServerLogic.CommunicationQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

                Tuple<Transaction,List<Transaction>> merged_log = Persistency.mergeTransactions(old_transactions);
                merged_log.getSecond().forEach(a -> this.delivery.add(a.getRequest()));

                Transaction to_handle = merged_log.getFirst();

                if (to_handle != null){
                    if (!to_handle.hasVoted()){
                        this.slave_log.abortTransaction(to_handle);
                    }
                    else{
                        this.slave_log.startTransactionMiddle(to_handle);
                    }
                    sync.setWait();

                    if (to_handle.commited()){
                        this.delivery.add(to_handle.getRequest());
                    }
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

    public static Tuple<Transaction,List<Transaction>> mergeTransactions(List<Transaction> list){
        Map<Integer,Transaction> aux_map = new HashMap<>();

        for(Transaction t : list){
            aux_map.put(t.getNumber(),t);
        }

        List<Transaction> all_transactions_merged = aux_map.values().stream().collect(Collectors.toList());
        //System.out.println(all_transactions_merged.get(0).toString());

        all_transactions_merged.sort((t1,t2)-> t1.getNumber() - t2.getNumber());

        List<Transaction> transactions_allready_completed = all_transactions_merged
                                                                .stream()
                                                                .filter(transaction -> transaction.completed())
                                                                .collect(Collectors.toList());

        Transaction last = null;
        if (transactions_allready_completed.size() != all_transactions_merged.size()){
            last = all_transactions_merged.subList(all_transactions_merged.size()-1,all_transactions_merged.size()).get(0);
        }

        return new Tuple<>(
                last,
                transactions_allready_completed
                        .stream()
                        .filter(transaction -> !transaction.hasAborted())
                        .collect(Collectors.toList()));


    }
}
