package me.alanx.concurrency.problems;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue2<T> {
    T[] array;
    int size = 0;
    int capacity;
    int head = 0;
    int tail = 0;
    // DON'T use a fair lock to give access to longer-waiting threads
    Lock lock = new ReentrantLock();
    Condition isEmpty = lock.newCondition();
    Condition isFull = lock.newCondition();
    Long timeout = 5L;
    public BlockingQueue2(int capacity) {
        array = (T[]) new Object[capacity];
        this.capacity = capacity;
    }
    public void enqueue(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (size == capacity) {
                // Use await() not wait()
                isFull.await(timeout, TimeUnit.MINUTES);
            }
            if (tail == capacity) {
                tail = 0;
            }
            array[tail] = item;
            tail++;
            size++;
        } finally {
            isEmpty.signalAll();
            lock.unlock();
        }
    }
    public T dequeue() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (size == 0) {
                isEmpty.await(timeout, TimeUnit.MINUTES);
            }
            if(head == capacity) {
                head = 0;
            }
            T i = array[head];
            array[head] = null;
            head++;
            size--;
            return i;
        } finally {
            isFull.signalAll();
            lock.unlock();
        }
    }
}