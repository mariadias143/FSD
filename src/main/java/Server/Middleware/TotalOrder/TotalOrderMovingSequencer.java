package Server.Middleware.TotalOrder;

import Server.ServerLogic.CommunicationQueue;
import io.atomix.utils.net.Address;
import Server.Middleware.Util.*;
import Server.Middleware.LeaderElection.*;

import java.util.*;

public class TotalOrderMovingSequencer implements Ordering {
    private Address[] other_peers;
    private int timestamp;
    private Queue<Message> messages_to_deliver;
    private CommunicationQueue<Message> operations_queue;
    private ServerUtil service;
    private Sequencer seq;
    private Address[] seq_addresses;
    private int n_messages_sent;
    private int idp;

    public TotalOrderMovingSequencer(Address[] other_peers, ServerUtil service, int id, Address[] sequencers,Map<Integer,Boolean> peer_status, CommunicationQueue queue){
        this.other_peers = other_peers;
        this.timestamp = 1;
        this.messages_to_deliver = new PriorityQueue<>();
        this.service = service;
        this.seq_addresses = sequencers;
        this.operations_queue = queue;
        this.n_messages_sent = 0;
        this.idp = id;

        if (peer_status.get(id) == true)
            this.seq = new Sequencer(service,other_peers,sequencers,id);
        else this.seq = null;

        service.ms.registerHandler("DELIVER",(a,b)->{
            Message m = this.service.s.decode(b);
            this.messages_to_deliver.add(m);

            List<Message> list = getMessagesReady();
            list.forEach(message -> this.operations_queue.add(message));

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
        int port = 12345 + this.idp;
        m.setId("MSG-" + port + "-" + n_messages_sent);
        this.n_messages_sent++;

        byte [] bytes = this.service.s.encode(m);
        for (int i = 0; i < seq_addresses.length; i++) {
            this.service.ms.sendAsync(this.seq_addresses[i],"TIMESTAMPING",bytes);
        }

    }

}
