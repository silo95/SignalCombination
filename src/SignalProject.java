
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class SignalProject {
    public final static int NUM_THREADS = 8;
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
    public static final double lossPercentage = 0.2;
    public static final double noiseMean = 0.0;
    public static final double noiseVariance = 1.0;
    public final static double MAX_SAMPLE_TIME = 1.0;
    public final static double samplingTime = 0.001;
    public final static Lock sharedLock = new ReentrantLock();
    public final static Condition finished = sharedLock.newCondition();
    public final static int queueCapacity = (int) Math.floor(MAX_SAMPLE_TIME / samplingTime);
    public static AtomicInteger writtenResults = new AtomicInteger(0);
    public static LinkedList<Sample> queue = new LinkedList<Sample>();
    public static PriorityQueue<Sample> resultQueue = 
            new PriorityQueue<Sample>(queueCapacity, new SampleComparator());
    public SignalProject(){
        this(Executors.newFixedThreadPool(NUM_THREADS));
    }
    public SignalProject(ExecutorService exec){
        //myExecutor = exec;
    }
    public static void main(String[] args){
        resPrinter = new ResultPrinter(sharedLock, finished, resultQueue,
                writtenResults, queueCapacity);
        SignalProject myInst;
        for(double i = 0; i<=MAX_SAMPLE_TIME; i+=samplingTime){
            myInst = new SignalProject();
            AtomicInteger counterSamples = new AtomicInteger(0);
            Lock myLock = new ReentrantLock();
            Condition cond = myLock.newCondition();
            LinkedList<Sample> myQueue = new LinkedList<Sample>();
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
        //myInst = new SignalProject();
        myExecutor.execute(resPrinter);
        myExecutor.shutdown();
    }
    private void makeResults(){
        myExecutor.execute(sigSample);
        myExecutor.execute(addNoise);
        myExecutor.execute(mulNoise);
        myExecutor.execute(resWr);
        
        //myExecutor.shutdown();
    }
}
