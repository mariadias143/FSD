package Server.Middleware.Util;

import Client.Reply.GetLastTopicsReply;
import Client.Reply.Reply;
import Client.Reply.WriteReply;
import Client.Request.*;
import Server.Middleware.LeaderElection.Election;
import Server.Middleware.LeaderElection.ElectionMessage;
import Server.Middleware.TotalOrder.Message;
import Server.Middleware.TotalOrder.Token;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
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
                .addType(Address.class)
                .addType(ElectionMessage.class)
                .addType(Reply.class)
                .addType(WriteReply.class)
                .addType(GetLastTopicsReply.class)
                .addType(RequestsI.class)
                .addType(GetLastTopics.class)
                .addType(PostMessage.class)
                .addType(SignIn.class)
                .addType(Subscribe.class)
                .addType(Message.class)
                .build();
    }


}