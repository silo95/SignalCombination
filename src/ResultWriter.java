
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

class ResultWriter implements Runnable, Callable<String>{
    private double signalValue;
    private double addValue;
    private double mulValue;
    private Sample addSample;
    private Sample mulSample;
    private Sample signalSample;
    private double result = -1.0;
    private double resTimestamp;
    private double printedTime;
    private final Lock lock;
    private final Condition cond;
    private final LinkedList<Sample> signalQueue;
    private final AtomicInteger diffSamples;
    private final Lock globalLock;
    private final Condition full;
    private final PriorityQueue<Sample> resultQueue;
    private final AtomicInteger writtenResults;
    private final int queueCapacity;
    
    public ResultWriter(Lock l, Condition c, double time, LinkedList<Sample> list,
            AtomicInteger count, Lock sharedLock, Condition co, PriorityQueue<Sample> pq,
            AtomicInteger ai, int capacity){
        lock = l;
        cond = c;
        signalQueue = list;
        diffSamples = count;
        resTimestamp = time;
        full = co;
        resultQueue = pq;
        writtenResults = ai;
        queueCapacity = capacity;
        globalLock = sharedLock;
    }
    @Override
    public void run() {
        //System.out.println(Thread.currentThread().getName() + " " + call());
        call();
    }

    @Override
    public String call(){
        lock.lock();
        try{
            while(diffSamples.get()<3){
                cond.await();
            }
            //Collections.sort(signalQueue, new SampleComparator());
            //for(int i=0; i<3; i++){
            globalLock.lock();
            try{
                for(int i=0; i<signalQueue.size(); i++){
                    Sample samp = signalQueue.get(i);
                    if(samp.recordedTimestamp == resTimestamp){
                        switch(samp.type){
                            case ADDNOISE:
                                addValue = samp.value;
                                addSample = samp;
                                break;
                            case MULNOISE:
                                mulValue = samp.value;
                                mulSample = samp;
                                break;
                            case SIGNAL:
                                signalValue = samp.value;
                                signalSample = samp;
                                break;
                        }
                    }
                }
                signalQueue.remove(addSample);
                signalQueue.remove(signalSample);
                signalQueue.remove(mulSample);
                double sum = signalValue + addValue;
                if(mulValue == 0 && sum < 0)
                    result = 0.0;
                else
                    result = (signalValue + addValue)*mulValue;
                Sample s = new Sample(resTimestamp, result, Sample.SignalType.RESULT);
                resultQueue.add(s);
                int ret = writtenResults.incrementAndGet();
                if(ret == queueCapacity){
                    full.signal();
                } 
            }
            finally{
                globalLock.unlock();
            }
            
        }catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        finally{
            lock.unlock();
        }
        return "Signal Result is: (" + resTimestamp + ", " + result + ")\n";
    }
    
}
