package Server.Middleware.TotalOrder;

import io.atomix.utils.net.Address;
import Server.Middleware.Util.*;
import Server.Middleware.LeaderElection.*;

import java.util.*;

public class TotalOrderMovingSequencer {
    private Election network;
    private int timestamp;
    private Queue<Message> messages_to_deliver;
    private ServerUtil service;
    private Sequencer seq;
    private Address[] seq_addresses;

    public TotalOrderMovingSequencer(Election e,ServerUtil service,int id,Address[] sequencers){
        this.network = e;
        this.timestamp = 1;
        this.messages_to_deliver = new PriorityQueue<>();
        this.service = service;
        this.seq_addresses = sequencers;

        this.seq = new Sequencer(service,e.other_peers,sequencers,id);

        service.ms.registerHandler("DELIVER",(a,b)->{
            Message m = this.service.s.decode(b);
            this.messages_to_deliver.add(m);

            List<Message> list = getMessagesReady();
            list.forEach(message -> System.out.println(message.getData()));

        },this.service.e);
    }

    private List<Message> getMessagesReady(){
        List<Message> list = new ArrayList<>();
        int i = this.messages_to_deliver.size();
        try{
            while (i > 0){
                Message m = this.messages_to_deliver.peek();
                if (m.getTimestamp() == this.timestamp){
                    m = this.messages_to_deliver.poll();
                    list.add(m);
                    this.timestamp++;
                }
                else{
                    break;
                }
                i--;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }


    public void send(Message m){
        byte [] bytes = this.service.s.encode(m);
        for (int i = 0; i < seq_addresses.length; i++) {
            this.service.ms.sendAsync(this.seq_addresses[i],"TIMESTAMPING",bytes);
        }

    }

}
