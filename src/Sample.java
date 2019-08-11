
import java.sql.Timestamp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author silo_
 */

//System.currentTimeMillis();
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
