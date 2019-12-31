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

    public boolean isWait() {
        lock.lock();
        try {
            return wait;
        }finally {
            lock.unlock();
        }


    }

    public void setWait(boolean wait) {
        lock.lock();
        try {
            this.wait = wait;
            if(!this.wait)
                c.signal();
            else
                c.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

}
