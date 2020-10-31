# ArcRoutingLibrary

[![DOI](https://zenodo.org/badge/11673331.svg)](https://zenodo.org/badge/latestdoi/11673331)

## Features

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
* Visualization Utilities
* OSM graph ingestion
* Decoupled Improvement Procedure Framework

## About the Author

Oliver Lum is currently a PhD Candidate in the University of Maryland, College Park's Applied Mathematics and Scientific Computation (AMSC) program. His research focus is in combinatorial optimization over networks, specifically with regard to arc routing problems.

## Background

After being tasked with writing a solver for a new arc routing problem, and having zero code base to work from, the author identified several inconveniences:

* First, although Java is fast becoming the industry standard for most new projects, the vast majority of robust graph libraries were composed for C/C++ (e.g. Boost, LEMON, etc.)
* Second, what libraries did exist were catered towards node-routing applications (consistent with literature), despite the close relationship between the two.
* Third, software development in research is (for a variety of legitimate reasons), fairly stove-piped, leading to time lost re-implementing other methods both for comparison, as well as to solve sub-problems.

This library is intended to be a collection of problem abstractions and solver implementations for some common arc-routing problems. We hope that its existence reduces the time it takes for new researchers to get started.

## License

This library is released under the MIT license. For more details, see the [License](LICENSE.txt)

## Tutorial

For some example code snippets, check out [GeneralTestbed.java](src/oarlib/test/GeneralTestbed.java).

## Special Thanks

Any amount of success that this project may achieve would not have been possible without the guidance and aid of the following individuals:

* Dr. Bruce Golden (Advisor)
* Dr. Angel Corberan
* Dr. Zaw Win
* Dr. Kriangchai Yaoyuenyong
* Dr. Vincente Campos
