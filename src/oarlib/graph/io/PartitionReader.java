package oarlib.graph.io;

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
