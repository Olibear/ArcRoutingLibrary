package oarlib.graph.io;

import oarlib.exceptions.UnsupportedFormatException;

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
	public boolean writeGraph(String filename) throws UnsupportedFormatException
	{
		//TODO
		switch(mFormat)
		{
		case OARLib:
			return writeOarlibGraph(filename);
		case Campos:
			break;
		case Corberan:
			break;
		case DIMACS_Modified:
			break;
		case Simple:
			break;
		case Yaoyuenyong:
			break;
		default:
			break;
		}
		throw new UnsupportedFormatException("While the format seems to have been added to the Format.Name type list,"
				+ " there doesn't seem to be an appropriate write method assigned to it.  Support is planned in the future," +
				"but not currently available");
	}
	public boolean writeOarlibGraph(String filename)
	{
		return false;
	}

}
