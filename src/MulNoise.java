import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class MulNoise implements Runnable, Callable<String>{
    private double samplingTime;
    private double lossPercentage;
    public Lock lock;
    public Condition cond;
    public AtomicInteger diffSamples;
    public LinkedList<Sample> signalQueue;
    public MulNoise(Lock l, Condition c, double sampTime, LinkedList<Sample> queue,
            AtomicInteger count, double percentage){
        samplingTime = sampTime;
        signalQueue = queue;
        diffSamples = count;
        lock = l;
        cond = c;
        lossPercentage = percentage;
    }
    public void run(){
        //System.out.println(Thread.currentThread().getName() + " " + call());
        call();
    }

    @Override
    public String call(){
        lock.lock();
        double value;
        try{
            Random rand = new Random();
            double res = rand.nextDouble();
            if(res <= lossPercentage){
                value = 0.0;
            }else{
                value = 1.0;
            }
            Sample samp = new Sample(samplingTime, value, Sample.SignalType.MULNOISE);
            /*boolean found = false;
            for(int i=0; i<signalQueue.size();i++){
                Sample s = signalQueue.get(i);
                if(s.type == Sample.SignalType.MULNOISE){
                   found = true;
                   break;
                }         
            }*/
            signalQueue.add(samp);
            int ret = -1;
            //if(!found){
                ret = diffSamples.incrementAndGet();
            //}
            //System.out.println(Thread.currentThread().getName() + " mul diffSamples= " + ret);
            if(ret == 3)
                cond.signal();
        }finally{
            lock.unlock();
        }
        return "MulNoise added: (" + samplingTime + ", " + value + ")\n";
    }
}
