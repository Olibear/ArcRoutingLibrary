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
package oarlib.problem.impl.io;

import oarlib.exceptions.FormatMismatchException;
import oarlib.exceptions.UnsupportedFormatException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * @author oliverlum
 */
public class PartitionReader {
    private PartitionFormat.Name mFormat;

    public PartitionReader(PartitionFormat.Name format) {
        mFormat = format;
    }

    public PartitionFormat.Name getFormat() {
        return mFormat;
    }

    public void setFormat(PartitionFormat.Name newFormat) {
        mFormat = newFormat;
    }

    public HashMap<Integer, Integer> readPartition(String fileName) throws UnsupportedFormatException {
        try {
            switch (mFormat) {
                case METIS:
                    return readMETISPartition(fileName);
                default:
                    throw new UnsupportedFormatException("This format is not currently supported for partitions.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    private HashMap<Integer, Integer> readMETISPartition(String fileName) throws FormatMismatchException {
        try {
            //ans
            HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();

            //file reading vars
            String line;
            String[] temp;
            int part;
            Integer counter = 1;
            File partitionFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(partitionFile));

            while ((line = br.readLine()) != null) {
                temp = line.split(",\\s+|:");
                part = Integer.parseInt(temp[0]);

                ans.put(counter, part);
                counter++;
            }

            br.close();
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
