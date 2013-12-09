package oarlib.graph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.MixedEdge;
import oarlib.exceptions.FormatMismatchException;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
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
	public Graph<?,?> readGraph(String fileName) throws UnsupportedFormatException, FormatMismatchException
	{
		switch (mFormat)
		{
		case Simple:
			return readSimpleGraph(fileName);
		case DIMACS_Modified:
			return null;
		case Corberan:
			return readCorberanGraph(fileName);
		}
		throw new UnsupportedFormatException("While the format seems to have been added to the Format.Name type list,"
				+ " there doesn't seem to be an appropriate reader method assigned to it.");
	}
	private Graph<?,?> readCorberanGraph(String fileName) throws FormatMismatchException
	{
		try{
			String line;
			String type = "";
			String[] temp;
			File graphFile = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(graphFile));
			//header info
			int n = 0;
			int m = 0;
			while((line = br.readLine()) != null)
			{
				if(line.contains("NOMBRE"))
				{
					temp = line.split("\\s+|:");
					if(temp[3].startsWith("MA") || temp[3].startsWith("MB"))
						type = "Mixed";
				}
				else if(line.contains("VERTICES"))
				{
					temp = line.split("\\s+|:");
					n = Integer.parseInt(temp[3]);
				}
				else if(line.contains("ARISTAS"))
				{
					temp = line.split("\\s+|:");
					m = Integer.parseInt(temp[3]);
					break;
				}
			}

			if(n == 0 || m == 0)
			{
				br.close();
				throw new FormatMismatchException("We could not detect any vertices (edges) in the file.");
			}
			//now split off into types
			if(type == "Mixed")
			{
				MixedGraph ans = new MixedGraph();
				int tailId;
				int headId;
				int cost1;
				int cost2;
				for(int i = 0; i < n; i++)
				{
					ans.addVertex(new MixedVertex("original"));
				}
				HashMap<Integer, MixedVertex> ansVertices = ans.getInternalVertexMap();
				br.readLine();
				br.readLine();
				int index;
				while((line = br.readLine()) != null)
				{
					if(line.contains("ARISTAS"))
						break;
					temp = line.split("\\s+|:|\\)|,|\\(");
					index = 1;
					if(temp[index].isEmpty())
						index++;
					tailId = Integer.parseInt(temp[index++]);
					if(temp[index].isEmpty())
						index++;
					if(temp[index].isEmpty())
						index++;
					headId = Integer.parseInt(temp[index++]);
					index+=2;
					cost1 = Integer.parseInt(temp[index++]);
					cost2 = Integer.parseInt(temp[index]);
					if(cost1 == 99999999) //backwards arc
					{
						ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(headId), ansVertices.get(tailId)), cost2, true));
					}
					else if(cost2 == 99999999) //forwards arc
					{
						ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(tailId), ansVertices.get(headId)), cost1, true));
					}
					else // edge
					{
						ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(tailId), ansVertices.get(headId)), cost1, false));
					}
				}
				br.close();
				return ans;
			}
			else
			{
				br.close();
				throw new FormatMismatchException("We don't currently support the type of graph right now.");
			}
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	private Graph<?,?> readSimpleGraph(String fileName) throws FormatMismatchException
	{
		try {
			String type; //first line of DIMACS_Modified
			String header; //second line of DIMACS_Modified
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
			if(type.equals("Directed"))
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

			else if(type.equals( "Undirected"))
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

			else if(type.equals("Mixed"))
			{
				//TODO
			}

			else if (type.equals("Windy"))
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
