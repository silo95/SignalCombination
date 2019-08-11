import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

class ResultPrinter implements Runnable, Callable<String>{
    private final Lock lock;
    private final Condition full;
    private final PriorityQueue<Sample> queue;
    private final AtomicInteger writtenResults;
    private final int queueCapacity;
    private String msg = "";
    public ResultPrinter(Lock l, Condition c, PriorityQueue<Sample> pq,
            AtomicInteger ai, int capacity){
       lock = l;
       full = c;
       queue = pq;
       writtenResults = ai;
       queueCapacity = capacity;
    }
    @Override
    public void run() {
        System.out.println(call());
    }

    @Override
    public String call(){
        lock.lock();
        try{
            while(writtenResults.get() < queueCapacity){
                full.await();
            }
            int size = queue.size();
            FileOutputStream os = new FileOutputStream("results.csv");
            for(int i=0; i<size; i++){
                Sample s = queue.poll();
                DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.GERMAN);
                dfs.setDecimalSeparator(',');
                dfs.setGroupingSeparator('.');
                DecimalFormat df = new DecimalFormat("####.##########");
                String res = df.format(s.recordedTimestamp) + ";" + df.format(s.value) + "\n";
                byte[] strToBytes = res.getBytes();
                //System.out.println("(" + s.recordedTimestamp + ", " + s.value + ")");
                msg += "(" + df.format(s.recordedTimestamp) + " ; " + df.format(s.value) + ")\n";
                os.write(strToBytes);
            }
        }catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }catch (IOException ioe){
            System.err.println("ERROR while writing file!");
        }
        finally{
            lock.unlock();
        }
        return "Printed result:\n" + msg;
    }
    
}
