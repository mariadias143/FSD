package utils.Rep;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.Collection;

public class GetTenPosts implements Reply {
     private Collection<String> messages;
     private String topic;

     public GetTenPosts(Collection<String> messages, String topic){
         this.messages=messages;
         this.topic=topic;
     }

    public Collection<String> getMessages() {
        return messages;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void sender(ManagedMessagingService ms, Address address, Serializer s) {
        ms.sendAsync(Address.from(address.host(), address.port()), "GET", s.encode(this))
                .thenRun(() -> {
                    System.out.println("Pedido recebido");
                });
    }
}
