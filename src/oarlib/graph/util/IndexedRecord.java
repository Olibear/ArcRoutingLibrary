package oarlib.graph.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Created by oliverlum on 10/21/14.
 *
 * Here S represents the key type, and T the value type.
 */
public class IndexedRecord<S extends Comparable>{

    private HashMap<Integer, S> mMap;
    private S record;
    private int recordKey;
    private Objective myObj;
    private boolean recordSet;

    public enum Objective {
        MAX,
        MIN
    }

    public IndexedRecord(Objective o) {
        myObj = o;
        recordSet = false;
        mMap = new HashMap<Integer, S>();
    }

    public void addEntry(int index, S value) {

        //add the entry
        mMap.put(index, value);

        //update the record if necessary
        if(!recordSet) {
            record = value;
            recordKey = index;
            recordSet = true;
        }
        else if(myObj == Objective.MAX && value.compareTo(record) > 0) {
            record = value;
            recordKey = index;
        }
        else if(myObj == Objective.MIN && value.compareTo(record) < 0) {
            record = value;
            recordKey = index;
        }

    }

    public S getEntry(int index) throws NoSuchElementException{
        if(! mMap.containsKey(index))
            throw new NoSuchElementException("The key you're looking for doesn't seem to exist.");
        return mMap.get(index);
    }

    public S getRecord() {
        if(!recordSet)
            throw new NoSuchElementException();
        return record;
    }

    public int getRecordKey() {
        if(!recordSet)
            throw new NoSuchElementException();
        return recordKey;
    }

    public boolean hasKey(int index) {
        return mMap.containsKey(index);
    }
}
