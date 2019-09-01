import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;


public class SignalSample implements Runnable, Callable<String>{
    private double samplingTime;
    private final AtomicInteger diffSamples;
    private final Lock lock;
    private final Condition cond;
    private final LinkedList<Sample> signalQueue;
    private final double signalAmplitude;
    private final double signalFrequency;
    private final double signalPhase;
    public SignalSample(Lock l, Condition c, double sampTime, LinkedList<Sample> queue,
            AtomicInteger count, double amplitude, double frequency, double phase){
        samplingTime = sampTime;
        signalQueue = queue;
        diffSamples = count;
        lock = l;
        cond = c;
        signalAmplitude = amplitude;
        signalFrequency = frequency;
        signalPhase = phase;
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
            value = signalAmplitude * Math.sin(2*Math.PI*signalFrequency*samplingTime
                + signalPhase);
            Sample samp = new Sample(samplingTime, value, Sample.SignalType.SIGNAL);
            signalQueue.add(samp);
            int ret = -1;
            ret = diffSamples.incrementAndGet();
            //System.out.println(Thread.currentThread().getName() + " signal diffSamples: " + ret);
            if(ret == 3)
                cond.signal();
        }finally{
            lock.unlock();
        }
        return "SignalSample added: (" + samplingTime + ", " + value + ")\n";
    }
}
