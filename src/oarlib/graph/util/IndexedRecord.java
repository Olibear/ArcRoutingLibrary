/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.graph.util;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Created by oliverlum on 10/21/14.
 * <p/>
 * Here S represents the key type, and T the value type.
 */
public class IndexedRecord<S extends Comparable> {

    private HashMap<Integer, S> mMap;
    private S record;
    private int recordKey;
    private Objective myObj;
    private boolean recordSet;

    public IndexedRecord(Objective o) {
        myObj = o;
        recordSet = false;
        mMap = new HashMap<Integer, S>();
    }

    public void addEntry(int index, S value) {

        //add the entry
        mMap.put(index, value);

        //update the record if necessary
        if (!recordSet) {
            record = value;
            recordKey = index;
            recordSet = true;
        } else if (myObj == Objective.MAX && value.compareTo(record) > 0) {
            record = value;
            recordKey = index;
        } else if (myObj == Objective.MIN && value.compareTo(record) < 0) {
            record = value;
            recordKey = index;
        }

    }

    public S getEntry(int index) throws NoSuchElementException {
        if (!mMap.containsKey(index))
            throw new NoSuchElementException("The key you're looking for doesn't seem to exist.");
        return mMap.get(index);
    }

    public S getRecord() {
        if (!recordSet)
            throw new NoSuchElementException();
        return record;
    }

    public int getRecordKey() {
        if (!recordSet)
            throw new NoSuchElementException();
        return recordKey;
    }

    public boolean hasKey(int index) {
        return mMap.containsKey(index);
    }

    public enum Objective {
        MAX,
        MIN
    }
}
