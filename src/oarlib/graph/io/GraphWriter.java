package oarlib.graph.io;
/**
 * Writer to output various file formats.  Plans to use Gephi for visualization.
 * @author Oliver
 *
 */
public class GraphWriter {
	private Format.Name mFormat;
	public GraphWriter(Format.Name format)
	{
		mFormat = format;
	}
	public Format.Name getFormat()
	{
		return mFormat;
	}
	public void setFormat(Format.Name newFormat)
	{
		mFormat = newFormat;
	}
	public boolean writeGraph(String filename)
	{
		//TODO
		return false;
	}

}
