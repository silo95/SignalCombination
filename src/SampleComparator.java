import java.util.*;

public class SampleComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        Sample s1 = (Sample)o1;
        Sample s2 = (Sample)o2;
        if(s1.recordedTimestamp == s2.recordedTimestamp)
            return 0;
        else if(s1.recordedTimestamp > s2.recordedTimestamp)
            return 1;
        else
            return -1;
    }
    
}
