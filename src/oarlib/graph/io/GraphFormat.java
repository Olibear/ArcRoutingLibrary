package oarlib.graph.io;

/**
 * Contains supported formats and format information for conversion / export / import
 *
 * @author Oliver
 */
public class GraphFormat {
    //Names of supported formats
    public enum Name {
        Corberan, //http://www.uv.es/corberan/instancias.htm
        DIMACS_Modified, //not supported
        Simple, // format of the Blossom V test instances
        Yaoyuenyong, // format of the instances used to test Yaoyuenyong's SAPH
        Campos, // format of the instances used in the paper "A Computational Study of Several Heuristics for the DRPP"
        METIS,
        OARLib
    }
}
