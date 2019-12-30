package Client.Request;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

public class SignIn implements RequestsI {
    private String request;
    private String username;
    private String password;
    private Address ip;
    private int server_id;
    private String r_class;

    public SignIn(String username,String password){
        this.username = username;
        this.password = password;
        this.request = "POST";

        this.r_class = "SignIn";
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
}
