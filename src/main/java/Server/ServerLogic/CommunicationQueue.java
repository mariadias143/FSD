package Server.ServerLogic;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommunicationQueue<T> {
    private Lock l;
    private Condition newarrivals;
    private Queue<T> requests;

    public CommunicationQueue(){
        this.l = new ReentrantLock();
        this.newarrivals = this.l.newCondition();
        this.requests = new ArrayDeque<>();
    }

    public T get() throws InterruptedException{
        T res = null;

        try{
            this.l.lock();
            while (requests.isEmpty()){
                this.newarrivals.await();
            }
            res = this.requests.poll();
        }
        finally {
            this.l.unlock();
        }

        return res;
    }

    public void add(T item){
        try{
            this.l.lock();
            this.requests.add(item);
            this.newarrivals.signalAll();
        }
        finally {
            this.l.unlock();
        }
    }
}
