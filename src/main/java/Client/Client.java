package Client;

import Client.Presentation.ClientUI;
import Client.Request.*;

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
import java.util.Set;
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

    public Client(String myAddress, String serverAddress) {

        this.myAddress = Address.from(myAddress);
        this.forwarderAddress = Address.from(serverAddress);

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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void register (String username, String password) {
        Request request = new SignIn(username,password);
        request.sender(ms, forwarderAddress, s);

    }

    public boolean verifyCredentials(){
        return this.username != null && this.password!=null;
    }


    public void getMessages(){
        if(verifyCredentials()){
            Request request = new GetLastTopics(this.username,this.password);
            request.sender(ms,forwarderAddress,s);
        }
        System.out.println("Falta fazer SetUp");
    }

    public void postMessage(Set<String> topics, String message) {
        if (verifyCredentials()) {
            Request request = new PostMessage(this.username, this.password, message, topics);
            request.sender(ms,forwarderAddress,s);
        }
        else
            System.out.println("Falta fazer SetUp");

    }

    public void postTopics (Set<String> topics) {
        if (verifyCredentials()) {
            Request request = new Subscribe(this.username, this.password, topics);
            request.sender(ms, forwarderAddress, s);
        }
        else
            System.out.println("Falta fazer SetUp");

    }


    public void shutdown(){
        this.ms.stop();
    }


    public static void main(String[] args) throws Exception {

        int option;
        Client client = new Client(args[0], args[1]);

        ClientUI clientUI = new ClientUI(client);

        while(true) {
            clientUI.showMenuInicial();
            option = clientUI.read_menu_output();
            if(option==0) {
                client.shutdown();
                break;

            }
        }
    }

}

