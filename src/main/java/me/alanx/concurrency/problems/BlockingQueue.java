package me.alanx.concurrency.problems;

public class BlockingQueue<T> {
    T[] array;
    int size = 0;
    int capacity;
    int head = 0;
    int tail = 0;
    Object lock = new Object();
    public BlockingQueue(int capacity) {
        array = (T[]) new Object[capacity];
        this.capacity = capacity;
    }
    public void enqueue(T item) throws InterruptedException {
        synchronized(lock) {
            while (size == capacity) {
                lock.wait();
            }
            if (tail == capacity) {
                tail = 0;
            }
            array[tail] = item;
            tail++;
            size++;
            /*
             * Why notifyAll?
             * Imagine capacity of queue is 5, initial size is 0.
             * There are 1 thread trying to dequeue and blocked
             * because queue is empty;
             * Then there are n nodes trying to enqueue, because
             * queue is empty initially, one enqueue thread will
             * win and put 1 item and n - 1 thread are blocking to
             * compete the lock;
             * Before the enqueue thread exit, it notify() another
             * 1 thread. Because the lock is not fair, it tends
             * to select another enqueue thread, and the other thread
             * nofity another enqueue thread and the situation repeat
             * util the queue is full and the next selected enqueue
             * thread is blocked too;
             * Thus we have both enqueue and dequeue blocked, which is
             * a dead lock;
             * The situation for the dequeue thread not be able to get
             * CPU is called Starvation.
             *
             * With object monitor, what we can do is to wakeup all
             * threads to increase the chance for the starving dequeue
             * thread to get access to the lock. But it is not guarranteed
             * for starving dequeue thread to win, as all threads will compete
             * on the lock;
             * Also, we can give wait() a timeout and put it in a while loop
             * to avoid deadlock;
             * And the lock should be able identify repeatedly
             * waiting threads and choose other threads, by the means above,
             * we should be able avoid deadlock;
             *
             * Another throughts is to use a Fair Lock which is implemented
             * by ReentrantLock to give access by time of waiting. But it has
             * issue, imagine 100 enqueue threads comes to a queue with cap of 5,
             * then a dequeue thread come latest. It will be dead dead.
             */
            lock.notifyAll();
        }
    }
    public T dequeue() throws InterruptedException {
        synchronized (lock) {
            while (size == 0) {
                lock.wait();
            }
            if(head == capacity) {
                head = 0;
            }
            T i = array[head];
            /*
             * Without this line, the queue could still work, but it might
             * cause issue on GC.
             */
            array[head] = null;
            head++;
            size--;
            lock.notifyAll();
            return i;
        }
    }
}