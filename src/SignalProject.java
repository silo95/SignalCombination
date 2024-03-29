import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class SignalProject {
    public final static int NUM_THREADS = 20;
    private final static ExecutorService myExecutor = 
            Executors.newFixedThreadPool(NUM_THREADS);
    private SignalSample sigSample;
    private AdditiveNoise addNoise;
    private MulNoise mulNoise;
    private ResultWriter resWr;
    private static ResultPrinter resPrinter;
    public static final double signalFrequency = 10;
    public static final double signalAmplitude = 2;
    public static final double signalPhase = 0;
    public static final double lossPercentage = 0.3;
    public static final double noiseMean = 0.0;
    public static final double noiseVariance = 1.0;
    public final static double MAX_SAMPLE_TIME = 100.0;
    public final static double samplingTime = 0.001;
    public final static Lock sharedLock = new ReentrantLock();
    public final static Condition finished = sharedLock.newCondition();
    public final static int queueCapacity = (int) Math.floor(MAX_SAMPLE_TIME / samplingTime);
    public static AtomicInteger writtenResults = new AtomicInteger(0);
    public static PriorityQueue<Sample> resultQueue = 
            new PriorityQueue<Sample>(queueCapacity, new SampleComparator());

    public static void main(String[] args){
        resPrinter = new ResultPrinter(sharedLock, finished, resultQueue,
                writtenResults, queueCapacity);
        SignalProject myInst;
        AtomicInteger counterSamples;
        Lock myLock;
        Condition cond;
        LinkedList<Sample> myQueue;
        for(double i = 0; i<=MAX_SAMPLE_TIME; i+=samplingTime){
            myInst = new SignalProject();
            counterSamples = new AtomicInteger(0);
            myLock = new ReentrantLock();
            cond = myLock.newCondition();
            myQueue = new LinkedList<Sample>();
            myInst.sigSample = new SignalSample(myLock, cond, i, myQueue,
                    counterSamples, signalAmplitude, signalFrequency, signalPhase);
            myInst.addNoise = new AdditiveNoise(myLock, cond, i, myQueue,
                    counterSamples, noiseMean, noiseVariance);
            myInst.mulNoise = new MulNoise(myLock, cond, i, myQueue,
                    counterSamples, lossPercentage);
            myInst.resWr = new ResultWriter(myLock, cond, i, myQueue,
                    counterSamples, sharedLock, finished, resultQueue, writtenResults, queueCapacity);
            myInst.makeResults();
        }
        myExecutor.execute(resPrinter);
        myExecutor.shutdown();
    }
    private void makeResults(){
        myExecutor.execute(sigSample);
        myExecutor.execute(addNoise);
        myExecutor.execute(mulNoise);
        myExecutor.execute(resWr);
    }
}
