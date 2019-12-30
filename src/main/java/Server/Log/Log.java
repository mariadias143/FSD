package Server.Log;

import Server.Middleware.TotalOrder.Message;
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
    private Message message;
    private Set <Character> state;
    private SegmentedJournal privateLog;
    private Serializer s;


    public Log(String myPort){
        s = new SerializerBuilder().build();
        privateLog = SegmentedJournal.<String>builder()
                .withName(myPort + "Log")
                .withSerializer(s)
                .build();

        this.state=new HashSet<>();
    }

    public void addState(char state) {
        this.state.add(state);
        if(state=='A'){
            resetLog();
        }

    }

    public void setMessage(Message message,char state) {
        this.message=message;
        this.addState(state);
    }

    public void resetLog(){
        this.message=null;
        this.state= new HashSet<>();

    }



    public void commit(){
        addState('C');

        SegmentedJournalWriter<String> w = privateLog.writer();
        w.append("messagem");
        CompletableFuture.supplyAsync(()->{w.flush();return null;})
                .thenRun(()->{
                    w.close();
                    System.out.println("ESCREVI NO log");
                });

    }

    public List<String> read(){
        List<String> state= new ArrayList<>();
        SegmentedJournalReader<String> r = privateLog.openReader(0);
        while(r.hasNext()) {
            Indexed<String> e = r.next();
            System.out.println(e.index()+": "+e.entry());
            state.add(e.index()+": "+e.entry());
        }
        r.close();
        return state;
    }

}
