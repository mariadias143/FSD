package Server.Log;


import Server.Middleware.TotalOrder.Message;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import org.apache.commons.collections4.Get;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
/*
public class Slaves extends Thread{
    private boolean coordinator;
    private Address addressCoordinator;
    private ExecutorService e;
    private Address myPort;
    private Address [] network;
    private Log log;
    private List<String> requests;
    private int transaction;


  public Slaves(String myPort, Address [] network) {
      this.myPort = Address.from("0.0.0.0", Integer.parseInt(myPort));
      this.network = network;
      log = new Log(myPort);
      requests = new ArrayList<>();
      requests = log.read();

          ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

          ManagedMessagingService ms = new NettyMessagingService("Process", this.myPort, new MessagingConfig());
          ms.start();
          Serializer s = new SerializerBuilder().build();

          ms.registerHandler("PREPARED", (a, b) -> {

              Message m = s.decode(b);
              try {

                  if (!coordinator) {
                      this.log.setMessage(m,'P');
                      ms.sendAsync(this.addressCoordinator, "OK", s.encode("OK"));
                  }

              } catch (Exception ex){
                  this.log.setMessage(m,'A');
                  ms.sendAsync(this.addressCoordinator, "ABORT", s.encode("ABORT"));
              }

          }, e);


          ms.registerHandler("COMMIT", (a, b) -> {
              this.log.commit();
          }, e);

          ms.registerHandler("ROLLBACK",(a,b)->{
                this.log.addState('A');
          },e);


      }


}


 */