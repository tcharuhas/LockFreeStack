/*****************************************LOCK-FREE STACK STRESS TESTS*****************************************

The purpose of this file is to explore the benefits of implementing a lock-free algorithm, more specifically, to a stack.  To do this, 
we will be comparing the amount of operations per 1/2 second with a more "classic" conccurent stack.


Note on atomic classes in java:
-------------------------------
Java has many classes that provide lock-free programming options.  Specifically,
I will be using the java.util.concurrent.atomic package.  This packages contains
classes and features which provide concurrency options without using locks.

What is AtomicReference?
--------------------------
This class provides a reference to the underlying object references which can be
read and written atomically.  In this sense, atomic is to mean that reads from and writes to these 
variables are thread safe.



******************************************************************************************************************/

import java.io.*; 
import java.util.List; 
import java.util.ArrayList; 
import java.util.Random; 
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.concurrent.atomic.AtomicReference; 
import java.util.concurrent.locks.LockSupport; 
  
class LockFree { 
  
    public static void main(String[] args) 
        throws InterruptedException 
    { 
  
        // defining two different stacks - LockFree and a Classic stack w/ a lock
  
        LockFreeStack<Integer> currStack = new LockFreeStack<Integer>(); 
        
        Random randomIntegerGenerator = new Random(); 
  
        // filling the stacks with random values
        for (int j = 0; j < 10; j++) { 
            currStack.push(Integer.valueOf( 
                randomIntegerGenerator.nextInt())); 

        } 
  
        // defining threads for Stack Operations, 2 threads for each stack
        List<Thread> threads = new ArrayList<Thread>(); 
        int stackPushThreads = 2; 
        int stackPopThreads = 2; 
  
        for (int k = 0; k < stackPushThreads; k++) { 
            Thread pushThread = new Thread(() -> { 
                System.out.println("Pushing into Lock-Free stack..."); 

                while (true) { 

                    currStack.push(Integer.valueOf(randomIntegerGenerator.nextInt()));
                } 

            }); 
  
            // making the threads low priority before 
            // starting them 
            pushThread.setDaemon(true); 
            threads.add(pushThread); 
        } 
  
        // putting the two stacks through a 'pop' stress test
        for (int k = 0; k < stackPopThreads; k++) { 
            Thread popThread = new Thread(() -> { 
                System.out.println( "Popping from Lock-Free stack ..."); 
                while (true) { 
                    currStack.pop(); 
                } 
            }); 

  
           // setting the threads as daemon threads so they can run even in the background
            popThread.setDaemon(true); 
            threads.add(popThread); 
         
        } 
  
        for (Thread thread : threads) { 
            thread.start(); 
        } 

        Thread.sleep(500); 
  
        System.out.println( "The number of stack operations performed in 1/2 a second for the Lock-Free stack-->" + currStack.getNoOfOperations()); 
    } 
  
    // class implementation for our lockfree stack
    private static class LockFreeStack<T> { 
        // defining the nodes in our stack as an atomic reference
        private AtomicReference<StackNode<T> > headNode = new AtomicReference<StackNode<T> >(); 
        private AtomicInteger noOfOperations = new AtomicInteger(0); 

        // will be critical in our comparison of stack operations
        public int getNoOfOperations() 
        { 
            return noOfOperations.get(); 
        } 
  
        // push operation 
        public void push(T value) 
        { 
            StackNode<T> newHead = new StackNode<T>(value); 
  
            // CAS loop defined 
            while (true) { 
                StackNode<T> currentHeadNode = headNode.get(); 
                newHead.next = currentHeadNode; 
  
                // perform CAS operation before setting new 
                // value 
                if (headNode.compareAndSet(currentHeadNode, newHead)) { 
                    break; 
                } 
                else { 
                    // waiting for a nanosecond 
                    LockSupport.parkNanos(1); 
                } 
            } 
  
            // getting the value atomically 
            noOfOperations.incrementAndGet(); 
        } 
  
        // pop function 
        public T pop() 
        { 
            StackNode<T> currentHeadNode = headNode.get(); 
  
            // CAS loop defined 
            while (currentHeadNode != null) { 
                StackNode<T> newHead = currentHeadNode.next; 
                if (headNode.compareAndSet(currentHeadNode, newHead)) { 
                    break; 
                } 
                else { 
                    // waiting for a nanosecond 
                    LockSupport.parkNanos(1); 
                    currentHeadNode = headNode.get(); 
                } 
            } 
            noOfOperations.incrementAndGet(); 
            return currentHeadNode != null ? currentHeadNode.value : null; 
        } 
    } 
  
    // class implementation of a normal stack for concurrency
    private static class ClassicStack<T> { 
  
        private StackNode<T> headNode; 
  
        private int noOfOperations; 
  
        // we need to synchronize the operations
        public synchronized int getNoOfOperations() 
        { 
            return noOfOperations; 
        } 
  
        public synchronized void push(T number) 
        { 
            StackNode<T> newNode = new StackNode<T>(number); 
            newNode.next = headNode; 
            headNode = newNode; 
            noOfOperations++; 
        } 
  
        public synchronized T pop() 
        { 
            if (headNode == null) 
                return null; 
            else { 
                T val = headNode.getValue(); 
                StackNode<T> newHead = headNode.next; 
                headNode.next = newHead; 
                noOfOperations++; 
                return val; 
            } 
        } 
    } 
  
    private static class StackNode<T> { 
        T value; 
        StackNode<T> next; 
        StackNode(T value) { 
            this.value = value; 
        } 
  
        public T getValue() { 
            return this.value; 
         } 
    } 
}
