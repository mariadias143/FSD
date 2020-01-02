package Server.Middleware.LeaderElection;

import io.atomix.utils.net.Address;
import Server.Middleware.Util.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Election {
    public Peer leader;
    public Peer current_node;
    public Address[] other_peers;
    public boolean election_active;
    public ServerUtil service;
    public Lock l;

    public Election(int id,Address[] peers,ServerUtil server){
        this.leader = null;
        this.other_peers = peers;
        this.current_node = new Peer(id,other_peers[id]);
        this.election_active = false;
        this.service = server;
        this.l = new ReentrantLock();

        this.service.ms.registerHandler("START_ELECTION",(a,b) ->{
            try{
                l.lock();
                if (this.election_active == false){
                    this.election_active = true;
                    int clockwise_neighbor = this.clockwise_neighbor();
                    ElectionMessage msg = new ElectionMessage(current_node.id_peer);

                    this.service.ms.sendAsync(this.other_peers[clockwise_neighbor],"RUN_ELECTION",this.service.s.encode(msg));
                }
            }
            finally {
                l.unlock();
            }
        },this.service.e);

        this.service.ms.registerHandler("RUN_ELECTION",(a,b)->{
            try{
                l.lock();
                ElectionMessage msg = this.service.s.decode(b);
                if (msg.id > this.current_node.id_peer){
                    ElectionMessage msg_to_forward = new ElectionMessage(msg.id);
                    int clockwise_neighbor = this.clockwise_neighbor();
                    this.service.ms.sendAsync(this.other_peers[clockwise_neighbor],"RUN_ELECTION",this.service.s.encode(msg_to_forward));
                }
                else if (msg.id == this.current_node.id_peer){
                    this.election_active = false;
                    this.leader = new Peer(msg.id,this.other_peers[msg.id]);
                    for (int i = 0; i < this.other_peers.length; i++) {
                        if (!this.other_peers[i].equals(this.current_node.ip_peer))
                            this.service.ms.sendAsync(this.other_peers[i],"COMMIT_ELECTION",this.service.s.encode(msg));
                    }
                }
            }
            finally {
                l.unlock();
            }
        },this.service.e);

        this.service.ms.registerHandler("COMMIT_ELECTION",(a,b)->{
            try{
                l.lock();
                ElectionMessage msg = this.service.s.decode(b);
                this.leader = new Peer(msg.id,this.other_peers[msg.id]);
                this.election_active = false;
                notifyAll();

                System.out.println("Sou o " + current_node.id_peer + " e o leader é " + msg.id);
            }
            finally {
                l.unlock();
            }

        },this.service.e);

    }

    public int clockwise_neighbor(){
        return this.current_node.id_peer == (other_peers.length - 1) ? 0 : (this.current_node.id_peer + 1);
    }

    public int anticlockwise_neighbor(){
        return this.current_node.id_peer == 0 ? this.other_peers.length - 1 : (this.current_node.id_peer - 1);
    }

    public void init_election(){
        try{
            l.lock();
            ElectionMessage msg = new ElectionMessage(0);
            for (int i = 0; i < this.other_peers.length; i++) {
                service.ms.sendAsync(this.other_peers[i],"START_ELECTION",this.service.s.encode(msg));
            }
        }
        finally {
            l.unlock();
        }
    }

    public boolean haveLeader(){
        boolean flag = false;
        try{
            l.lock();
            flag = this.leader != null && this.election_active == false;
        }
        finally {
            l.unlock();
        }

        return flag;
    }

    public boolean isLeader(){
        boolean flag = false;
        try{
            l.lock();
            if (this.leader == null){
                flag = false;
            }
            else
                flag = this.leader.id_peer == this.current_node.id_peer;
        }
        finally {
            l.unlock();
        }
        return flag;
    }

    public void waitleader(){
        try{
            this.l.lock();
            while (this.leader == null){
                wait();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            this.l.unlock();
        }
    }
}
