package Client.Presentation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Block {
    private boolean wait;
    private ReentrantLock lock = new ReentrantLock();
    private Condition c = lock.newCondition();

    public  Block(){
        this.wait = false;
    }

    public void awake(){
        try{
            lock.lock();
            this.wait = false;
            c.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void setWait() {
        try {
            lock.lock();
            this.wait = true;
            while (this.wait)
                this.c.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
