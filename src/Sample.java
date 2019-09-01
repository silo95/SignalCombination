import java.sql.Timestamp;

class Sample {
    public enum SignalType{SIGNAL, ADDNOISE, MULNOISE, RESULT};
    public double recordedTimestamp;
    public double value;
    public SignalType type; 
    public Sample(double timestamp, double val, SignalType t){
        recordedTimestamp = timestamp;
        value = val;
        type = t;
    }
}
