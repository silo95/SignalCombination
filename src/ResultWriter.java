
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
    private final Condition full;
    private final PriorityQueue<Sample> resultQueue;
    private final AtomicInteger writtenResults;
    private final int queueCapacity;
    
    public ResultWriter(Lock l, Condition c, double time, LinkedList<Sample> list,
            AtomicInteger count, Condition co, PriorityQueue<Sample> pq,
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
            for(int i=0; i<signalQueue.size(); i++){
                //Sample samp = signalQueue.poll();
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
                /*printedTime = samp.recordedTimestamp;
                switch(samp.type){
                    case ADDNOISE:
                        addValue = samp.value;
                        break;
                    case SIGNAL:
                        signalValue = samp.value;
                        break;
                    case MULNOISE:
                        mulValue = samp.value;
                        break;
                    default:
                        System.out.println("ERROR: value not found!\n");
                }*/
            }
            signalQueue.remove(addSample);
            signalQueue.remove(signalSample);
            signalQueue.remove(mulSample);
            double sum = signalValue + addValue;
            if(mulValue == 0 && sum < 0)
                result = 0.0;
            else
                result = (signalValue + addValue)*mulValue;
            //diffSamples.set(0);
            /*if(signalQueue.peekFirst()!=null){
                boolean signalFound = false;
                boolean addFound = false;
                boolean mulFound = false;
                for(int i=0; i<signalQueue.size(); i++){
                    if(addFound && signalFound && mulFound)
                        break;
                    Sample s = signalQueue.get(i);
                    switch(s.type){
                        case ADDNOISE:
                            if(!addFound){
                                diffSamples.incrementAndGet();
                                addFound = true;
                            }
                            break;
                        case SIGNAL:
                            if(!signalFound){
                                diffSamples.incrementAndGet();
                                signalFound = true;
                            }
                            break;
                        case MULNOISE:
                            if(!mulFound){
                                diffSamples.incrementAndGet();
                                mulFound = true;
                            }
                            break;
                    }
                }
            }*/
            /*int ret = diffSamples.get();
            System.out.println(Thread.currentThread().getName() + " res diffSamples: " + ret);
            if(ret==3)
                    cond.signal();*/
            Sample s = new Sample(resTimestamp, result, Sample.SignalType.RESULT);
            resultQueue.add(s);
            int ret = writtenResults.incrementAndGet();
            if(ret == queueCapacity){
                full.signal();
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
