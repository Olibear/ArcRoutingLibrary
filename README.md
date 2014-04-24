#ArcRoutingLibrary

##Status Disclaimer

This project is still in development, and in an unstable state currently.  Features planned for development:

* Visualization Utilities (high priority)
* Multi-Vehicle Solvers (medium priority)
* OSM graph ingestion (medium priority)
* Decoupled Improvement Procedure Framework (low priority)

Features Completed:

* Collection of Common Algorithms
* DCPP Solver (Edmonds)
* DCPP Solver (Gurobi IP implementation)
* UCPP Solver (Edmonds)
* UCPP Solver (Gurobi IP implementation)
* MCPP Heuristic (Frederickson)
* MCPP Heuristic (Yaoyuenyong)
* WRPP Heuristic (WRPP1 Benavent)
* WRPP Heuristic (H1 Benavent)
* DRPP Heuristic (Christofides)

Ongoing Development:

* Graph Architecture (on-going development)
* More Parsers / Format Conversion (on-going)
* Integrate Faster Open Source Implementations of Existing Algorithms (on-going)
* My Own Research (on-going)

##About the Author

Oliver Lum is currently a 2nd year graduate student in the University of Maryland, College Park's Applied Mathematics and Scientific Computation (AMSC) program.  His research focus is in combinatorial optimization over networks, specifically with regard to vehicle routing and scheduling.  Since high school, he has spent his summers doing software development on two DARPA programs (TIGR and TransApps), where he now supports several defense-related efforts.  He graduated with a B.S. in Mathematics, and a B.A. in Philosophy in 2012.  

##Background

One of the requirements of the AMSC program is that each student must complete a software project during his / her 2nd year.  The specific idea to create this library arose after identifying several inconveniences: 
* First, although Java is fast becoming the industry standard for most new projects, the vast majority of robust graph libraries were composed for C/C++ (e.g. Boost, LEMON, etc.)
* Second, what libraries did exist were catered towards node-routing applications (consistent with literature), despite the close relationship between the two.
* Third, software development in research is (for a variety of legitimate reasons), fairly stove-piped, leading to time lost re-implementing other methods both for comparison, as well as to solve sub-problems.  Having a standard representation will help reduce clutter.

This library is intended to be a collection of problem abstractions and solver implementations for some common arc-routing problems.  

##License

This library is released under the MIT license. For more details, see the License.txt

##Tutorial

For some example code snippts, check out GeneralTestbed.java .  

## Special Thanks

Any amount of success that this project may achieve would not have been possible without the guidance and aid of the following individuals:

* Dr. Bruce Golden (Advisor)
* Dr. Angel Corberan
* Dr. Zaw Win
* Dr. Kriangchai Yaoyuenyong
* Dr. Vincente Campos