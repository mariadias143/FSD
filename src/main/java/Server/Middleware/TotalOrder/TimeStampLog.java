package Server.Middleware.TotalOrder;

import Server.Middleware.Util.ServerUtil;
import io.atomix.storage.journal.Indexed;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TimeStampLog {
    private SegmentedJournal privateLog;
    private Serializer s;

    public TimeStampLog(int idp, ServerUtil service){
        this.s = service.s;
        privateLog = SegmentedJournal.<String>builder()
                .withName("TimeStamp-Log-" + String.valueOf(idp))
                .withSerializer(s)
                .build();
    }

    public void add(int timestamp){
        SegmentedJournalWriter<Object> w = privateLog.writer();
        w.append(timestamp);
        CompletableFuture.supplyAsync(()->{w.flush();return null;})
                .thenRun(()->{
                    System.out.println("Atualizado log para o timestamp");
                });
    }

    public int hasCrashed(){
        List<Integer> timestamp_values = new ArrayList<>();
        SegmentedJournalReader<Integer> r = privateLog.openReader(0);
        while(r.hasNext()) {
            Indexed<Integer> e = r.next();
            timestamp_values.add(e.entry());
        }

        if (timestamp_values.size() == 0)
            return -1;
        return Collections.max(timestamp_values);
    }
}
