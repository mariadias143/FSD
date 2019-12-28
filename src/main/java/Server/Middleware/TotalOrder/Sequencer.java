package Server.Middleware.TotalOrder;

import io.atomix.utils.net.Address;

import Server.Middleware.Util.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Iterator;

public class Sequencer {
        private ServerUtil service;
        private Address[] peers;
        private Address[] other_sequencers;
        private Queue<Message> messages_to_send;
        private int id;

        public Sequencer(ServerUtil service,Address[] peers,Address[] other_seq,int id){
            this.service = service;
            this.peers = peers;
            this.other_sequencers = other_seq;
            this.id = id;
            this.messages_to_send = new ArrayDeque<>();

            this.service.ms.registerHandler("TIMESTAMPING",(a,b)->{
                Message m = this.service.s.decode(b);
                messages_to_send.add(m);

            },this.service.e);

            this.service.ms.registerHandler("TOKEN",(a,b)->{
                Token t = this.service.s.decode(b);

                try{
                    Iterator it = this.messages_to_send.iterator();

                    while (it.hasNext()){
                        Message m = (Message) it.next();
                        if (!t.contains(m.getId())){
                            m.setTimestamp(t.getSeqNum());
                            t.addId(m.getId());

                            broadcast(m);
                        }
                    }
                }
                catch (Exception ee){ee.printStackTrace();}

                this.messages_to_send = new ArrayDeque<>();
                sendToken(t);
                
            },this.service.e);

            if (id == 0){
                Token t = new Token();
                this.service.ms.sendAsync(other_sequencers[0],"TOKEN",this.service.s.encode(t));
            }
        }

        private void broadcast(Message m){
            byte [] m_bytes = this.service.s.encode(m);

            for (int i = 0; i < peers.length; i++) {
                this.service.ms.sendAsync(peers[i],"DELIVER",m_bytes);
            }
        }

        private void sendToken(Token t){
            int nextSequencer = (id + 1) % this.other_sequencers.length;
            byte [] m_bytes = this.service.s.encode(t);

            this.service.ms.sendAsync(this.other_sequencers[nextSequencer],"TOKEN",m_bytes);
        }
}