
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class SignalProject {
    private final ExecutorService myExecutor;
    private SignalSample sigSample;
    private AdditiveNoise addNoise;
    private MulNoise mulNoise;
    private ResultWriter resWr;
    private static ResultPrinter resPrinter;
    //nuovo commento
    public static final double signalFrequency = 128;
    public static final double signalAmplitude = 2;
    public static final double signalPhase = 0;
    public static final double lossPercentage = 0.2;
    public static final double noiseMean = 0.03;
    public static final double noiseVariance = 1.5;
    public final static double MAX_SAMPLE_TIME = 1.0;
    public final static double samplingTime = 0.2;
    public final static Lock sharedLock = new ReentrantLock();
    public final static Condition finished = sharedLock.newCondition();
    public final static int queueCapacity = (int) Math.floor(MAX_SAMPLE_TIME / samplingTime);
    public static AtomicInteger writtenResults = new AtomicInteger(0);
    public static LinkedList<Sample> queue = new LinkedList<Sample>();
    public static PriorityQueue<Sample> resultQueue = 
            new PriorityQueue<Sample>(queueCapacity, new SampleComparator());
    public SignalProject(){
        this(Executors.newCachedThreadPool());
    }
    public SignalProject(ExecutorService exec){
        myExecutor = exec;
    }
    public static void main(String[] args){
        resPrinter = new ResultPrinter(sharedLock, finished, resultQueue,
                writtenResults, queueCapacity);
        SignalProject myInst;
        for(double i = 0; i<=MAX_SAMPLE_TIME; i+=samplingTime){
            myInst = new SignalProject();
            AtomicInteger counterSamples = new AtomicInteger(0);
            Condition cond = sharedLock.newCondition();
            myInst.sigSample = new SignalSample(sharedLock, cond, i, queue,
                    counterSamples, signalAmplitude, signalFrequency, signalPhase);
            myInst.addNoise = new AdditiveNoise(sharedLock, cond, i, queue,
                    counterSamples, noiseMean, noiseVariance);
            myInst.mulNoise = new MulNoise(sharedLock, cond, i, queue,
                    counterSamples, lossPercentage);
            myInst.resWr = new ResultWriter(sharedLock, cond, i, queue,
                    counterSamples, finished, resultQueue, writtenResults, queueCapacity);
            myInst.makeResults();
        }
        myInst = new SignalProject();
        myInst.myExecutor.execute(resPrinter);
        myInst.myExecutor.shutdown();
    }
    private void makeResults(){
        myExecutor.execute(sigSample);
        myExecutor.execute(addNoise);
        myExecutor.execute(mulNoise);
        myExecutor.execute(resWr);
        
        myExecutor.shutdown();
    }
}
