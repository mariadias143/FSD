package Client.Request;

import Client.Client;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.List;

public  class Request {
    private String request;
    private String username;
    private String password;
    private Address ip;
    private int server_id;

    public Request(String username,String password){
        this.username = username;
        this.password = password;
        this.request = "";
    }

    public void setAddress(Address ip){
        this.ip = ip;
    }

    public Address getIp() {
        return ip;
    }

    public  void setRequest(String request){
        this.request=request;
    }

    public void sender(ManagedMessagingService ms, Address address, Serializer s) {
        ms.sendAsync(address, request, s.encode(this))
                .thenRun(() -> {
                    System.out.println("Pedido enviado");
                });
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
}
