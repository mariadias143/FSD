package Server.Log;

import Server.Middleware.TotalOrder.Message;
import Server.Middleware.Util.ServerUtil;
import io.atomix.storage.journal.Indexed;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Log {
    private Transaction transaction;
    private ArrayList <String> state;
    private SegmentedJournal privateLog;
    private Serializer s;


    public Log(String myPort, ServerUtil service){
        s = service.s;
        privateLog = SegmentedJournal.<String>builder()
                .withName(myPort + "Log")
                .withSerializer(s)
                .build();

        this.state=new ArrayList<>();
    }


    public void addState(String state, Transaction t) {
        this.transaction = t;
        SegmentedJournalWriter<Object> w = privateLog.writer();
        this.transaction.addState(state);
        w.append(this.transaction);

        CompletableFuture.supplyAsync(()->{w.flush();return null;});

        if(state.equals("A")){
            resetLog();
        }

    }

    public void setMessage(Transaction t,String state) {
        this.transaction=t;
        this.addState(state,t);
    }

    public void resetLog(){
        this.transaction=null;
        this.state= new ArrayList<>();
    }


    public void commit(Transaction t){
        addState("C",t);
        SegmentedJournalWriter<Object> w = privateLog.writer();
        this.transaction.setState(this.state);
        w.append(this.transaction);
        CompletableFuture.supplyAsync(()->{w.flush();return null;})
                .thenRun(()->{
                    System.out.println("ESCREVI NO log");
                });
    }

    public List<Transaction> read(){
        List<Transaction> state= new ArrayList<>();
        SegmentedJournalReader<Transaction> r = privateLog.openReader(0);
        while(r.hasNext()) {
            Indexed<Transaction> e = r.next();
            System.out.println(e.index()+": "+e.entry());
            state.add(e.entry());
        }
        r.close();
        return state;
    }

}
