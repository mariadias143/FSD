package Server.Log;


import Client.Presentation.Block;
import Client.Request.RequestsI;
import Server.Middleware.LeaderElection.Election;
import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import Server.Server;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.storage.journal.Indexed;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import org.apache.commons.collections4.Get;

import javax.lang.model.type.ArrayType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Slaves {
    private Election network;
    private ServerUtil service;
    private String myPort;
    private Address [] peers;
    private Transaction transaction;
    private Log log;
    private boolean active_transaction;
    private Queue<Integer> requests;
    private Lock l;
    private Block sync;

  public Slaves(String myPort, Address [] peers, Election network, ServerUtil service) {
      this.myPort = myPort;
      this.peers = peers;
      this.network = network;
      log = new Log(myPort,service);
      this.service = service;
      this.active_transaction = false;
      this.requests = new ArrayDeque<>();
      this.l = new ReentrantLock();

      this.service.ms.registerHandler("PREPARE", (a, b) -> {
          try{
              l.lock();
              int id = this.service.s.decode(b);
              if(active_transaction && this.transaction.getNumber()==id){
                  Address leader = network.leader.ip_peer;
                  try {
                      this.log.addState("P",this.transaction);
                      this.service.ms.sendAsync(leader, "OK", this.service.s.encode(this.transaction.getNumber()));
                  } catch (Exception ex){
                      this.log.addState("A",this.transaction);
                      this.service.ms.sendAsync(leader, "ABORT", this.service.s.encode(this.transaction.getNumber()));
                  }
              }
              else{
                  requests.add(this.transaction.getNumber());
              }
          }
          finally {
              l.unlock();
          }

      }, this.service.e);


      this.service.ms.registerHandler("COMMIT", (a, b) -> {
          System.out.println("Recebi pedido COMMIT");
          int id = this.service.s.decode(b);
          try{
              l.lock();
              if(active_transaction && this.transaction.getNumber()==id){
                  this.log.commit(this.transaction);
                  this.active_transaction = false;
                  this.service.ms.sendAsync(network.leader.ip_peer,"ACK",this.service.s.encode(transaction.getNumber()));//apagar em caso de error.
                  this.sync.awake();
              }
          }
          finally {
              l.unlock();
          }

      }, this.service.e);

      this.service.ms.registerHandler("ROLLBACK", (a, b) -> {
          int id = this.service.s.decode(b);
          try{
              l.lock();
              if(active_transaction && this.transaction.getNumber()==id){
                  this.log.addState("A",this.transaction);
                  this.active_transaction = false;
                  this.service.ms.sendAsync(network.leader.ip_peer,"ACK",this.service.s.encode(transaction.getNumber()));
                  this.sync.awake();
              }
          }
          finally {
              l.unlock();
          }
      }, this.service.e);



  }

  public synchronized void setBlock(Block sync){
      this.sync = sync;
  }

  public void startTransaction(Transaction t){
      try{
          l.lock();
          this.transaction = t;
          this.active_transaction = true;
          Address leader = network.leader.ip_peer;
          this.log.addState("I",t);

          if (!this.requests.isEmpty()){
              this.requests.poll();
              this.service.ms.sendAsync(leader, "OK", this.service.s.encode(this.transaction.getNumber()));
          }
      }
      finally {
          l.unlock();
      }
  }

    public void startTransactionMiddle(Transaction t){
        try{
            l.lock();
            this.transaction = t;
            this.active_transaction = true;
        }
        finally {
            l.unlock();
        }
    }

  public List<Transaction> transactions_started(){
      List<Transaction> res = new ArrayList<>();
      SegmentedJournal log = SegmentedJournal.<String>builder()
              .withName(myPort + "Log")
              .withSerializer(service.s)
              .build();
      SegmentedJournalReader<Transaction> r = log.openReader(0);

      while(r.hasNext()) {
          Indexed<Transaction> e = r.next();
          System.out.println(e.index()+": "+e.entry().getRequest().getData().toString() + " - " + e.entry().getNumber());
          res.add( e.entry());
      }


      r.close();

      return res;
  }

  public void abortTransaction(Transaction t){
      this.log.addState("A",t);
      this.service.ms.sendAsync(network.leader.ip_peer,"ABORT",this.service.s.encode(t.getNumber()));
  }

  public boolean hasCrashed(){
      SegmentedJournal log = SegmentedJournal.<String>builder()
              .withName(myPort + "Log")
              .withSerializer(service.s)
              .build();
      SegmentedJournalReader<Transaction> r = log.openReader(0);

      if (r.hasNext())
          return true;
      return false;
  }



}


