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

/**
 * Contains supported formats and format information for conversion / export / import
 *
 * @author Oliver
 */
public class ProblemFormat {
    //Names of supported formats
    public enum Name {
        Corberan, //http://www.uv.es/corberan/instancias.htm
        Simple, // format of the Blossom V test instances
        Yaoyuenyong, // format of the instances used to test Yaoyuenyong's SAPH
        Campos, // format of the instances used in the paper "A Computational Study of Several Heuristics for the DRPP"
        METIS, // format of the input / output of the METIS graph partitioning library
        MeanderingPostman, // format used by researcher Rui Zhang to store the WRPP with ZigZag option, AKA the Meandering Postman Problem
        OARLib,
        Zhang_Matrix
    }
}
