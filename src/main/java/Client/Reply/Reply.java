package Client.Reply;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

public abstract class Reply {

    public abstract void printContent();

    public abstract String error();

    public void reply(ManagedMessagingService ms, Address add, Serializer s){
        ms.sendAsync(add,"REP",s.encode(this)).thenRun(() -> {
            System.out.println("Resposta enviada.");
        });
    }
}
