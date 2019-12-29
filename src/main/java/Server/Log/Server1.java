package Server.Log;

import Client.Request.Get;
import Client.Request.Post;
import Client.Request.Request;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Server1 extends Thread{
    private boolean coordinator;
    private Address addressCoordinator;
    private ExecutorService e;
    private Address myPort;
    private Address [] network;
    private Log log;
    private List<String> requests;
    private int transaction;


  public Server1(String myPort, Address [] network) {
      this.myPort = Address.from("0.0.0.0", Integer.parseInt(myPort));
      this.network = network;
      log = new Log(myPort);
      requests = new ArrayList<>();
      requests = log.read();
      this.transaction = requests.size();
      System.out.println(requests);

          ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

          ManagedMessagingService ms = new NettyMessagingService("Process", this.myPort, new MessagingConfig());
          ms.start();
          Serializer s = new SerializerBuilder().withTypes(Request.class, Get.class, Post.class,Transaction.class).build();



          ms.registerHandler("coordinator", (a, b) -> {
              String request = s.decode(b);//  decode da mensagem
              this.coordinator=true;
              this.addressCoordinator=this.myPort;
              //Caso seja coordenador
              for (Address address : network)
                  ms.sendAsync(address, "I'm your leader", s.encode("leader"));

          }, e);

          ms.registerHandler("I'm your leader", (a, b) -> {
              String request = s.decode(b);//  decode da mensagem
              this.addressCoordinator=a;

          }, e);



          ms.registerHandler("req", (a, b) -> {
              /**
              Request request = s.decode(b);//  decode da mensagem
                  System.out.println("sou o servidor á escuta na porta : " + this.myPort + " e recebi " + request.getRequest() +" e tenho num transacao "+ this.transaction );
                  //Caso seja coordenador
              if(coordinator) {
                  log.write(request.getRequest());
                      transaction++;
                  Transaction t = new Transaction(this.transaction,request.getRequest());

                  this.requests.add(request.getRequest());
                  for (Address address : network)
                      if (!address.equals(this.myPort)) {// nao enviar duas vezes para  o coordenador
                          ms.sendAsync(address, "transaction", s.encode(t));
                      }

              }*/
          }, e);


          ms.registerHandler("transaction", (a, b) -> {
              Transaction transaction = s.decode(b);//  decode da mensagem
              System.out.println("sou o servidor á escuta na porta : " + this.myPort + " e recebi uma transanção "+ " e tenho transacao " + this.transaction);
              if(transaction.getNumber()==this.transaction+1) {
                  log.write(transaction.getRequest());
                  this.requests.add(transaction.getRequest());
                  this.transaction++;
              }
              else{
                  ms.sendAsync(this.addressCoordinator,"MISS",s.encode(this.transaction+1));
              }
          }, e);


          ms.registerHandler("MISS", (a, b) -> {
              int numTransaction = s.decode(b);//  decode da mensagem
                List<String> transactions = this.log.read(numTransaction);
              for(String transaction : transactions ) {
                  Transaction t = new Transaction(numTransaction, transaction);
                  ms.sendAsync(a, "transaction", s.encode(t));
                  numTransaction++;

              }
              System.out.println("sou o servidor á escuta na porta : " + this.myPort + " e recebi um miss" +a );

          }, e);



      }





    public static void main(String[] args) {

        Address[] network = new Address[]{
                Address.from("0.0.0.0",12345),
                Address.from("0.0.0.0",12346),
                Address.from("0.0.0.0",12347),
                Address.from("0.0.0.0",12348)

        };
        new Server1(args[0],network);
        Serializer s = new SerializerBuilder().build();
        ExecutorService es = Executors.newSingleThreadExecutor();

        ManagedMessagingService ms = new NettyMessagingService(
                "starter",
                Address.from(23459),
                new MessagingConfig());
        ms.start();
      //  int coor = (int) (Math.random() * 4);
        ms.sendAsync(network[0], "coordinator", s.encode("coordinator"));
        // start all processes


    }






}
