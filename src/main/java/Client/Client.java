package Client;

import Client.Rep.*;
import Client.Req.*;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;





public class Client {
    private final Address myAddress;
    private final Address forwarderAddress;
    private final ManagedMessagingService ms;
    private final Serializer s;
    private ExecutorService e;

    public Client(String myAddress, String forwarderAddress) {
        this.myAddress = Address.from(myAddress);
        this.forwarderAddress = Address.from(forwarderAddress);
        this.e = Executors.newFixedThreadPool(1);
        this.ms = new NettyMessagingService(
                "teste",
                this.myAddress,
                new MessagingConfig());
        ms.start();
        this.s = new SerializerBuilder()
                .withTypes(Request.class, GetMessage.class, PostMessage.class, Subscribe.class)
                .build();

        ms.registerHandler("REPLY/messages", (a, b) -> {
            Reply reply = s.decode(b);//  decode da mensagem
            if (reply instanceof GetTenPosts) {
                System.out.println("Topic : " + ((GetTenPosts) reply).getTopic());
                for (String message : ((GetTenPosts) reply).getMessages()) {
                    System.out.println(message);
                }
            }

        }, e);

    }

    public void handlerInput(String input) {
        String[] header = input.split("/");
        switch (header[0]) {
            case "GET":
                handlerGET(header);
                break;
            case "POST":
                handlerPOST(header);
                break;

        }

    }


    public void handlerGET(String[] header) {
        if (header[1].equals("messages")) {
            Request req = new GetMessage();
            req.sender(ms, forwarderAddress, s);
        }
    }

    public void handlerPOST(String[] header) {
        if ((header.length >= 4) && header[0].equals("POST") && header[1].equals("message")) {
            sendPostMessage(header);
        } else {
            if (header.length >= 3 && header[0].equals("POST") && header[1].equals("subscribe")) {
                sendPostSubscribe(header);
            }
        }
    }


    public void sendPostMessage(String[] header) {
        String topic[] = header[2].split(" ");
        Collection<String> topics = new ArrayList<>();
        for (String top : topic) {
            topics.add(top);
        }

        String message = header[3];
        if (header.length > 4)// caso a mensagem contenha /
            for (int i = 4; i < header.length; i++)
                message.concat(header[i]);

        Request m1 = new PostMessage(message, topics);
        m1.sender(ms, forwarderAddress, s);

    }

    public void sendPostSubscribe(String[] header) {
        String topic[] = header[2].split(" ");
        Collection<String> topics = new ArrayList<>();
        for (String top : topic) {
            topics.add(top);
        }
        Request m2 = new Subscribe(topics);
        m2.sender(ms, forwarderAddress, s);

    }


    public static void main(String[] args) throws Exception {

        Client client = new Client(args[0], args[1]);

        String input;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while ((input = in.readLine()) != null) {
            client.handlerInput(input);

        /*
            GET/messages
            POST/message/topic/text
            POST/subscribe/topic
         */


        }
    }
}
