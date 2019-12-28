package Server.Middleware.TotalOrder;


import io.atomix.utils.net.Address;
import Server.Middleware.Util.*;

public class Leader {
    private ServerUtil service;
    private Address[] peers;
    private int timestamp = 1;

    public Leader(ServerUtil service,Address[] peers){
        this.service = service;
        this.peers = peers;

        service.ms.registerHandler("TIMESTAMPING",(a,b)->{
            Message m = this.service.s.decode(b);
            m.setTimestamp(timestamp);

            byte [] m_bytes = this.service.s.encode(m);

            for (int i = 0; i < peers.length; i++) {
                this.service.ms.sendAsync(peers[i],"DELIVER",m_bytes);
            }

            timestamp++;
        },this.service.e);
    }
}