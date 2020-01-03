package Server.Middleware.TotalOrder;

import Server.ServerLogic.CommunicationQueue;
import io.atomix.utils.net.Address;
import Server.Middleware.Util.*;
import Server.Middleware.LeaderElection.*;

import java.util.*;

public class TotalOrderFixedSequencer implements Ordering {
    private Election network;
    private int timestamp;
    private Queue<Message> messages_to_deliver;
    private Queue<Message> queue_to_send;
    private CommunicationQueue<Message> operations_queue;
    private ServerUtil service;
    private Leader leader_middleware;
    private TimeStampLog log;


    public TotalOrderFixedSequencer(Election e,ServerUtil service,CommunicationQueue queue){
        this.network = e;
        this.messages_to_deliver = new PriorityQueue<>();
        this.queue_to_send = new ArrayDeque<>();
        this.service = service;
        this.leader_middleware = new Leader(service,e.other_peers,network.current_node.ip_peer.port());
        this.operations_queue = queue;
        this.log = new TimeStampLog(network.current_node.ip_peer.port(),service,false);

        int control = this.log.hasCrashed();
        this.timestamp = control == -1 ? 1 : control;

        service.ms.registerHandler("DELIVER",(a,b)->{
            Message m = this.service.s.decode(b);
            this.messages_to_deliver.add(m);

            List<Message> list = getMessagesReady();
            list.forEach(message -> this.operations_queue.add(message));

            this.log.add(this.timestamp);

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
    //private Sequencer

    public void send(Message m){
        if(!network.haveLeader()){
            this.queue_to_send.add(m);
        }
        else {
            Address leader_address = this.network.leader.ip_peer;
            if (this.queue_to_send.size() != 0){
                for (int i = 0; i < this.queue_to_send.size(); i++) {
                    Message m_2 = this.queue_to_send.poll();
                    this.service.ms.sendAsync(leader_address,"TIMESTAMPING",this.service.s.encode(m_2));
                }
            }
            this.service.ms.sendAsync(leader_address,"TIMESTAMPING",this.service.s.encode(m));
        }
    }
}