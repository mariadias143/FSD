package Server;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Server.Middleware.TotalOrder.Message;

public class Bot {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        ManagedMessagingService ms = new NettyMessagingService("Wannabe twitter",
                Address.from(port),
                new MessagingConfig());

        Serializer s = new SerializerBuilder()
                .addType(Message.class)
                .build();

        ms.start();

        Address add = Address.from(port-12348+12345);

        Scanner tst = new Scanner(System.in);

        int id = port - 12348;

        System.out.println("Sou o " + id + " envio para" + add.toString());

        tst.nextLine();

        try{
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(25);
                String msg = port + "-" + i;
                Message m = new Message<String>(msg,0,msg);
                //Message m = new Message(msg,1,msg,1);
                ms.sendAsync(add,"msg",s.encode(m));
            }
        }
        catch (Exception kk){ }
    }
}