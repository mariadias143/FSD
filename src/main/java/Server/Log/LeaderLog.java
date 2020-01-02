package Server.Log;


import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LeaderLog {
    private Address myPort;
    private Log log;
    private Transaction transaction;
    private ServerUtil service;
    private List<Address> yesVoters;
    private int okAnswers;
    Address[] peers ;
    private boolean running_transaction;
    private int ack_received;
    private Map<Integer,Boolean> controlACK;

    public LeaderLog(String myPort, Address [] peers, ServerUtil s) {
        this.myPort = Address.from("0.0.0.0", Integer.parseInt(myPort));
        this.peers = peers;
        this.log = new Log("Coord-"  + myPort,s);
        this.yesVoters = new ArrayList<Address>();
        this.okAnswers = 0;
        this.service = s;
        this.running_transaction = false;
        this.ack_received = 0;

        this.service.ms.registerHandler("OK", (a, b) -> {
            if (running_transaction == true){
                System.out.println("Recebi OK");
                okAnswers++;
                yesVoters.add(a);
                if(okAnswers == peers.length) {
                    System.out.println("Recebi todos");
                    this.log.commit(this.transaction);
                    System.out.println("Dei commit");
                    for (Address p : peers)
                        this.service.ms.sendAsync(p, "COMMIT", this.service.s.encode(this.transaction.getNumber())).thenRun(() -> System.out.println("Enviei commit"));

                    this.service.e.schedule(()->{
                        this.verify_ACK();
                    }, 500 ,TimeUnit.MILLISECONDS);
                }
            }
        }, this.service.e);

        this.service.ms.registerHandler("ABORT", (a, b) -> {
            if (running_transaction == true) {
                for (Address add : peers)
                    this.service.ms.sendAsync(add, "ROLLBACK", this.service.s.encode(this.transaction.getNumber()));
                abortTransaction(this.transaction.getNumber());
            }
        }, this.service.e);

        this.service.ms.registerHandler("ACK",(a,b)->{
            int id = this.service.s.decode(b);
            if(running_transaction && this.transaction.getNumber()==id){
                int port = a.port();
                if (this.controlACK.get(port) == false){
                    ack_received++;
                }
                this.controlACK.put(port,true);
            }
        },this.service.e);
    }

    public void verify_ACK(){
        if (ack_received != peers.length) {
            for (Map.Entry<Integer, Boolean> keyval : this.controlACK.entrySet()) {
                if (keyval.getValue() == false) {
                    this.service.ms.sendAsync(Address.from(keyval.getKey()), "COMMIT", this.service.s.encode(this.transaction.getNumber()));
                }
            }
            this.service.e.schedule(()->{
                this.verify_ACK();
            }, 500 ,TimeUnit.MILLISECONDS);

        }
        else {
            this.running_transaction = false;
        }
    }

    public Map<Integer,Boolean> initControlMap(){
        Map<Integer,Boolean> res = new HashMap<>();
        for (int i = 0; i < this.peers.length; i++) {
            res.put(this.peers[i].port(),false);
        }

        return res;
    }

    public synchronized void openTransaction(Transaction t){
        try{
            while (running_transaction)
                wait();

            this.running_transaction = true;
            this.transaction = t;

            yesVoters = new ArrayList<>();
            okAnswers = 0;
            ack_received = 0;
            this.controlACK = initControlMap();

            for(Address p: peers)
                try {
                    this.service.ms.sendAsync(p, "PREPARE", this.service.s.encode(this.transaction.getNumber()));
                }
                catch (Exception ee){
                    ee.printStackTrace();
                }
            this.log.addState("P",this.transaction);
        }
        catch (Exception ee){
            ee.printStackTrace();
        }
    }

    public synchronized void abortTransaction(int TID){
        this.log.addState("A",this.transaction);
    }
}
