package Server.Middleware.LeaderElection;

import io.atomix.utils.net.Address;

public class Peer{
    public int id_peer;
    public Address ip_peer;

    public Peer(int id_peer,Address peer_ip){
        this.id_peer = id_peer;
        this.ip_peer = peer_ip;
    }
}