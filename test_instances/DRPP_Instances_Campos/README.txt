These data files are organized as follows:

line 1: total number of vertices in the graph
line 2: number of vertices that are incident with any required arc
line 3: total number of arcs (required and non required)
line 4: number of connected components induced by the set of required arcs
line 5: number of vertices at each connected component separated by commas
lines 6-end: arc list with associated cost and status of each arc, an
        status of 0 means non required arc, an status of 1 means required
        arc.
        Each one of these lines (6-end) represents only one arc with the format
                tail of the arc, head of the arc, cost, status
        For example the line:25, 12, 174, 1   means that there is an arc
        from vertex 25 to vertex 12 with cost 174 and this arc is required.

All the data are integer. Vertices are labeled with consecutive
integers, and the vertices incident with required arcs must be
labeled first with consecutive numbers 1,2,...,|Ar| (Ar is the
set of required arcs). The connected components induced by the
required arcs must follow this order too, i.e. if there is a
component with vertices 1,2 and 3, and there is another
component with vertices 4 and 5, for example, this will be
stated on line 5 as 3,2,....
