package Client;

import Client.Request.Get;
import Client.Request.Post;
import Client.Request.Request;

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
    private String username;
    private String password;

    public Client(String myAddress, String serverAddress,String username,String password) {

        this.myAddress = Address.from(myAddress);
        this.forwarderAddress = Address.from(serverAddress);
        this.username=username;
        this.password=password;

        this.e = Executors.newFixedThreadPool(1);
        this.ms = new NettyMessagingService(
                "Client",
                this.myAddress,
                new MessagingConfig());
        ms.start();

        this.s = new SerializerBuilder()
                .withTypes(Request.class, Get.class, Post.class)
                .build();


    }

    public void handlerInput(String input) {
        String[] header = input.split("/");
        switch (header[0]) {
            case "GET":
                sendGET(input);
                break;
            case "POST":
                sendPOST(input);
                break;
      }

    }


    public void sendGET(String request) {
        Request get = new Get(request);
        System.out.println(forwarderAddress);
        get.sender(ms, forwarderAddress, s);
    }


    public void sendPOST(String request) {
        Request post = new Post(request);
        System.out.println(forwarderAddress);
        post.sender(ms, forwarderAddress, s);
    }


    public static void main(String[] args) throws Exception {

        String input;
        String username;
        String password;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Username: ");
        username = in.readLine();
        System.out.println("Password: ");
        password= in.readLine();

        Client client = new Client(args[0], args[1],username,password);


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
