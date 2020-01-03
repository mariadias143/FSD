package Client.Request;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.HashSet;
import java.util.Set;

public class Subscribe implements RequestsI {
    private String request;
    private String username;
    private String password;
    private Address ip;
    private int server_id;
    private String r_class;
    private Set<String> topics;
    private boolean should_deliver;

    public Subscribe(String username,String password,Set<String> list_tops){
        this.topics = list_tops;
        this.username = username;
        this.password = password;
        this.request = "POST";

        this.r_class = "Subscribe";
        this.should_deliver = true;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public void setAddress(Address ip){
        this.ip = ip;
    }

    public Address getAddress() {
        return ip;
    }

    public String getUsername(){ return this.username; }

    public String getPassword() {
        return this.password;
    }

    public void setServer_id(int idp){
        this.server_id = idp;
    }

    public int getServer_id(){
        return this.server_id;
    }

    public String findClass(){
        return r_class;
    }

    public String findType(){
        return request;
    }

    public void send(ManagedMessagingService ms, Address address, Serializer s) {
        ms.sendAsync(address, request, s.encode(this))
                .thenRun(() -> {
                    System.out.println("Pedido enviado");
                });
    }

    public RequestsI clone(){
        RequestsI res = new Subscribe(this.username,this.password,new HashSet<>(this.topics));
        res.setAddress(this.ip);
        res.setServer_id(this.server_id);

        return res;
    }

    public boolean getDeliver() {
        return this.should_deliver;
    }

    public void changeDeliver() {
        this.should_deliver = false;
    }
}
