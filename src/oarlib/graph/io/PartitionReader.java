package oarlib.graph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

import oarlib.exceptions.FormatMismatchException;
import oarlib.exceptions.UnsupportedFormatException;

/**
 * 
 * @author oliverlum
 *
 */
public class PartitionReader {
	private PartitionFormat.Name mFormat;
	public PartitionReader(PartitionFormat.Name format)
	{
		mFormat = format;
	}
	public void setFormat(PartitionFormat.Name newFormat)
	{
		mFormat = newFormat;
	}
	public PartitionFormat.Name getFormat()
	{
		return mFormat;
	}
	public HashMap<Integer, HashSet<Integer>> readPartition(String fileName) throws UnsupportedFormatException
	{
		try
		{
			switch (mFormat)
			{
			case METIS:
				return readMETISPartition(fileName);
			default:
				throw new UnsupportedFormatException("This format is not currently supported for partitions.");
			}
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;

		}
	}
	private HashMap<Integer, HashSet<Integer>> readMETISPartition(String fileName) throws FormatMismatchException
	{
		try
		{
			//ans
			HashMap<Integer, HashSet<Integer>> ans = new HashMap<Integer, HashSet<Integer>>();

			//file reading vars
			String line;
			String[] temp;
			int part;
			Integer counter = 1;
			File partitionFile = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(partitionFile));
			
			while((line = br.readLine()) != null)
			{
				temp = line.split(",\\s+|:");
				part = Integer.parseInt(temp[0]);
				
				if(!ans.containsKey(part))
					ans.put(part, new HashSet<Integer>());
				
				ans.get(part).add(counter);
				counter++;
			}

			br.close();
			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
