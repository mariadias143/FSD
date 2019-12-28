package Server.Middleware.Util;

import Server.Middleware.LeaderElection.ElectionMessage;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.concurrent.ScheduledExecutorService;

public class ServerUtil {
    public ManagedMessagingService ms;
    public ScheduledExecutorService e;
    public Serializer s;

    public ServerUtil(ManagedMessagingService ms, ScheduledExecutorService e){
        this.ms = ms;
        this.e = e;
        this.s = new SerializerBuilder()
                .addType(ElectionMessage.class)
                .addType(Message.class)
                .addType(Token.class)
                .build();
    }
}