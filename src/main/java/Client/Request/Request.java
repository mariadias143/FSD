package Client.Request;

import Client.Client;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.List;

public  class Request {
    private String request;
    private String username;
    private String password;
    private Address ip;

    public Request(String username,String password){
        this.username = username;
        this.password = password;
        this.request = "";
    }

    public void setAddress(Address ip){
        this.ip = ip;
    }

    public  void setRequest(String request){
        this.request=request;
    }

    public void sender(ManagedMessagingService ms, Address address, Serializer s) {
        ms.sendAsync(address, request, s.encode(this))
                .thenRun(() -> {
                    System.out.println("Pedido enviado");
                });
    }
}
