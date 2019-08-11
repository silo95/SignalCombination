
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;


public class AdditiveNoise implements Runnable, Callable<String>{
    private double samplingTime;
    private final AtomicInteger diffSamples;
    private final Lock lock;
    private final Condition cond;
    private final LinkedList<Sample> signalQueue;
    private final double noiseMean;
    private final double noiseVariance;
    public AdditiveNoise(Lock l, Condition c, double sampTime, LinkedList<Sample> queue,
            AtomicInteger count, double mean, double variance){
        samplingTime = sampTime;
        signalQueue = queue;
        diffSamples = count;
        lock = l;
        cond = c;
        noiseMean = mean;
        noiseVariance = variance;
    }
    
    @Override
    public void run() {
        //System.out.println(Thread.currentThread().getName() + " " + call());
        call();
    }

    @Override
    public String call(){
        lock.lock();
        double value;
        try{
            Random rand = new Random();
            value = rand.nextGaussian()*Math.sqrt(noiseVariance) + noiseMean;
            /*double res = Math.random();
            if(res <= 0.5){
                value = -0.3;
            }else{
                value = 0.3;
            }*/
            Sample samp = new Sample(samplingTime, value, Sample.SignalType.ADDNOISE);
            
            /*boolean found = false;
            for(int i=0; i<signalQueue.size();i++){
                Sample s = signalQueue.get(i);
                if(s.type == Sample.SignalType.ADDNOISE){
                   found = true;
                   break;
                }         
            }*/
            signalQueue.add(samp);
            int ret = -1;
            //if(!found){
                ret = diffSamples.incrementAndGet();
            //}
            
            //System.out.println(Thread.currentThread().getName() + " add diffSamples= " + ret);
            
            if(ret == 3)
                cond.signal();
        }finally{
            lock.unlock();
        }
        return "Additive Noise added: (" + samplingTime + ", " + value + ")\n";
    }
}
