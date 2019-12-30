package Server;

import Client.Reply.GetLastTopicsReply;
import Client.Reply.Reply;
import Client.Reply.WriteReply;
import Client.Request.*;
import Server.Middleware.LeaderElection.ElectionMessage;
import Server.Middleware.TotalOrder.Token;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Server.Middleware.TotalOrder.Message;

public class Bot {
    public Scanner in = new Scanner(System.in);

    public Bot(){
        in = new Scanner(System.in);
    }

    public int readInt(){
        return in.nextInt();
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        ManagedMessagingService ms = new NettyMessagingService("Wannabe twitter",
                Address.from(port),
                new MessagingConfig());

        Serializer s = new SerializerBuilder()
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

        ms.start();

        Address add = Address.from(port-12348+12345);

        ms.registerHandler("REP",(a,b)->{
            Reply rep = s.decode(b);
            rep.printContent();
        },e);


        int id = port - 12348;

        System.out.println("Sou o " + id + " envio para" + add.toString());
        System.out.println("Porta: " + ms.address());


        /**
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
        catch (Exception kk){ }*/

        Address[] addresses = new Address[]{
                Address.from(12345),
                Address.from(12346),
                Address.from(12347),
        };

        new Thread(()->{
            Scanner iin = new Scanner(System.in);

            String user = "";
            String pass = "";

            while (true){
                System.out.println("OPCAO");
                int i = iin.nextInt();
                String kasd = iin.nextLine();
                switch (i){
                    case 0:{
                        System.out.println("Username:");
                        user = iin.nextLine();
                        System.out.println("Password:");
                        pass = iin.nextLine();
                        SignIn sig = new SignIn(user,pass);
                        sig.send(ms,add,s);

                        //ms.sendAsync(add,"POST",s.encode(sig));

                        break;}
                    case 1:{
                        System.out.println("MSG");
                        String msg = iin.nextLine();
                        System.out.println("NºTopicos");
                        int k = iin.nextInt();
                        kasd = iin.nextLine();
                        Set<String> sett = new HashSet<>();
                        for (int j = 0; j < k; j++) {
                            String topic = iin.nextLine();
                            sett.add(topic);
                        }
                        PostMessage pstmsg = new PostMessage(user,pass,msg,sett);
                        pstmsg.send(ms,add,s);
                        break;}
                    case 2:{
                        System.out.println("NºTopicos a subscrever:");
                        int k = iin.nextInt();
                        kasd = iin.nextLine();
                        Set<String> subs = new HashSet<>();
                        for (int j = 0; j < k; j++) {
                            String topic = iin.nextLine();
                            subs.add(topic);
                        }
                        Subscribe sb = new Subscribe(user,pass,subs);
                        sb.send(ms,add,s);
                        break;}
                    case 3:{
                        System.out.println("Ultimas 10 msgs");
                        GetLastTopics req = new GetLastTopics(user,pass);
                        req.send(ms,add,s);
                        break;}
                    case 4:{
                        for (int j = 0; j < 3; j++) {
                            ms.sendAsync(addresses[j],"TESTE",s.encode(new Message<>("",0,null)));
                        }
                    }
                    default:
                        break;
                }
            }
        }).start();

    }
}