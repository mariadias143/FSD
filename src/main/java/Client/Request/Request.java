package Client.Request;

import Client.Client;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.List;

public  class Request {
    private String request;

    public Request(){
        this.request = "";
    }
    public  void setRequest(String request){
        this.request=request;
    }

    public void sender(ManagedMessagingService ms, Address address, Serializer s) {
        ms.sendAsync(Address.from(address.host(), address.port()), request, s.encode(this))
                .thenRun(() -> {
                    System.out.println("Pedido enviado");
                });
    }


}
