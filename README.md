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



To test this out for yourself:
------------------------------------
1. Change the stack in main to whichever version you are interested in - lockfree or classic
2. Compile it - javac stack.java
3. Run it - java stack.java
