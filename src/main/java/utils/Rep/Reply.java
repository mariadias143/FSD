package utils.Rep;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

public interface Reply {
    public void sender(ManagedMessagingService ms, Address address, Serializer s);
}
