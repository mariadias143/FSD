package Server;

import io.atomix.storage.journal.Indexed;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Log {
    private SegmentedJournal privateLog;
    private Serializer s;

    public Log(String myPort){
        s = new SerializerBuilder().build();
        privateLog = SegmentedJournal.<String>builder()
                .withName(myPort + "Log")
                .withSerializer(s)
                .build();
    }



    public void write(String request){
        SegmentedJournalWriter<String> w = privateLog.writer();
        w.append(request);
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

    public List<String> read(int idTransaction){
        List<String> state= new ArrayList<>();
        SegmentedJournalReader<String> r = privateLog.openReader(idTransaction);
        while(r.hasNext()) {
            Indexed<String> e = r.next();
            state.add(e.index()+": "+e.entry());
        }
        r.close();
        return state;
    }
}
