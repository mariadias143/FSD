package Client.Request;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

public interface RequestsI {
    public void setAddress(Address ip);
    public Address getAddress();
    public void send(ManagedMessagingService ms, Address address, Serializer s);
    public String getUsername();
    public String getPassword();
    public void setServer_id(int idp);
    public int getServer_id();
    public String findClass();
    public String findType();
}
