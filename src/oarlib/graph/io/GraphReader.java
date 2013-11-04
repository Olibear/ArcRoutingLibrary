package oarlib.graph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.exceptions.FormatMismatchException;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

/**
 * Reader to accept various file formats, and store them as a graph object.  Plans to use Gephi.
 * @author Oliver
 *
 */
public class GraphReader {
	private Format.Name mFormat;
	public GraphReader(Format.Name format)
	{
		mFormat = format;
	}
	public void setFormat(Format.Name newFormat)
	{
		mFormat = newFormat;
	}
	public Format.Name getFormat()
	{
		return mFormat;
	}
	public Graph<?,?> readDirectedGraph(String fileName) throws UnsupportedFormatException, FormatMismatchException
	{
		switch (mFormat)
		{
		case DIMACS_Modified:
			return readDIMACSGraph(fileName);
		}
		throw new UnsupportedFormatException("While the format seems to have been added to the Format.Name type list,"
				+ " there doesn't seem to be an appropriate reader method assigned to it.");
	}
	private Graph<?,?> readDIMACSGraph(String fileName) throws FormatMismatchException
	{
		try {
			String type; //first line of DIMACS_Modified
			String header; //seoncd line of DIMACS_Modified
			File graphFile = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(graphFile));
			//header info
			type = br.readLine();
			if(type == null)
			{
				br.close();
				throw new FormatMismatchException("There were no readable lines in the file.");
			}
			header = br.readLine();
			if(header == null)
			{
				br.close();
				throw new FormatMismatchException("There was only one readable line in the file.");
			}
			String[] nm = header.split("\\s+");
			int n = Integer.parseInt(nm[0]);
			int m = Integer.parseInt(nm[1]);

			String line;
			String[] splitLine;
			
			//branch on types, (more elegant way?)
			if(type == "Directed")
			{
				DirectedGraph ans = new DirectedGraph();
				for(int i = 0; i < n; i++)
				{
					ans.addVertex(new DirectedVertex("Original"));
				}
				HashMap<Integer, DirectedVertex> indexedVertices =ans.getInternalVertexMap();
				for(int i =0; i < m-2; i++)
				{
					line = br.readLine();
					if(line == null)
					{
						br.close();
						throw new FormatMismatchException("There were not enough lines in the file to account for the number "
								+ "of edges claimed in the header.");
					}
					splitLine = line.split("\\s+");
					if(splitLine.length != 3)
					{
						br.close();
						throw new FormatMismatchException("One of the edge lines had too many entries in it.");
					}
					ans.addEdge(new Arc("Original", new Pair<DirectedVertex>(indexedVertices.get(Integer.parseInt(splitLine[0])), indexedVertices.get(Integer.parseInt(splitLine[1]))), Integer.parseInt(splitLine[2])));
				}
				if((line = br.readLine()) != null)
				{
					System.out.println("Ignoring excess lines in file.  This could just be whitespace, but there are more lines than "
							+ "are claimed in the header");
				}
				br.close();
				return ans;
			}
			
			else if(type == "Undirected")
			{
				UndirectedGraph ans = new UndirectedGraph();
				for(int i = 0; i < n; i++)
				{
					ans.addVertex(new UndirectedVertex("Original"));
				}
				HashMap<Integer, UndirectedVertex> indexedVertices =ans.getInternalVertexMap();
				for(int i =0; i < m-2; i++)
				{
					line = br.readLine();
					if(line == null)
					{
						br.close();
						throw new FormatMismatchException("There were not enough lines in the file to account for the number "
								+ "of edges claimed in the header.");
					}
					splitLine = line.split("\\s+");
					if(splitLine.length != 3)
					{
						br.close();
						throw new FormatMismatchException("One of the edge lines had too many entries in it.");
					}
					ans.addEdge(new Edge("Original", new Pair<UndirectedVertex>(indexedVertices.get(Integer.parseInt(splitLine[0])), indexedVertices.get(Integer.parseInt(splitLine[1]))), Integer.parseInt(splitLine[2])));
					
				}
				if((line = br.readLine()) != null)
				{
					System.out.println("Ignoring excess lines in file.  This could just be whitespace, but there are more lines than "
							+ "are claimed in the header");
				}
				br.close();
				return ans;
			}
			
			else if(type == "Mixed")
			{
				//TODO
			}
			
			else if (type == "Windy")
			{
				//TODO
			}
			//Something is wrong
			else
			{
				br.close();
				throw new FormatMismatchException("The type specified in the first line of the DIMACS_Modified file was not recognized."
						+ "  It should read either \"Directed\" \"Undirected\" \"Mixed\" or \"Windy\"");
			}
			br.close();
			return null;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
