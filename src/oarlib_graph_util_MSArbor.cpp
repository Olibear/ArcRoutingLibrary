#include <jni.h>
#include "oarlib_graph_util_MSArbor.h"
#include "MSArbor.h"
#include <stdio.h>
#include <iostream>

using namespace MSA_di_unipi_it;
using namespace std;

JNIEXPORT jintArray JNICALL Java_oarlib_graph_util_MSArbor_msArbor
(JNIEnv * env, jclass javaclass, jint n , jint m, jintArray costs)
{
  	//answer structure
	int ans[n-1];

  	//get things into a c friendly format
	jboolean j;
	int *weights = env->GetIntArrayElements(costs, &j);

  	//do the actual work here

  	// prepare & read costs- - - - - - - - - - - - - - - - - - - - - - - - - - -
  	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	/*
 	The format of the array:

    n = number of nodes: note that nodes are numbered from 0 to n - 1, and
        n - 1 is taken to be the root

    for( i = 0 ; i < n ; i++ )
     for( j = 0 ; j < n - 1 ; j++ )
      < cost of arc ( i , j ) >

    Note that arcs (i, n - 1), i.e., entering the root, will never be part
    of an optimal solution, so they are not even present in the file.
    */

	MSArbor::CRow csts = new MSArbor::CNumber[ n * ( n - 1 ) ];

	for( MSArbor::Index i = 0 ; i < n ; i++ )
	{
		register MSArbor::Index j = 0;
  		for( ; j < i ; j++ )             // j < i
  			csts[ n * j + i ] = weights[(n-1) * i + j];

  		if( i < n - 1 )                  // skip j == i and set c[i,i] = C_INF
 		{                                // but only if i < n - 1
		  	//MSArbor::CNumber foo;
		  	//inFile >> foo;
		  	csts[ n * (j++) + i ] = MSArbor::C_INF;

		   for( ; j < n - 1 ; j++ )        // i < j < n - 1
		   		csts[ n * j + i ] = weights[(n-1) * i + j];
		}
	}

	// construct the MSArbor object- - - - - - - - - - - - - - - - - - - - - - -

 	MSArbor MSA( n );

 	// solve the problem- - - - - - - - - - - - - - - - - - - - - - - - - - - -

 	MSA.Solve(csts);
 	for(int i = 0; i < n-1; i++)
 	{
 		ans[i] =  MSA.ReadPred()[ i ];
 	}

  	//go back into java readable structs
	jintArray arr = env->NewIntArray(n-1);
	env->SetIntArrayRegion(arr, 0,n-1,ans);
	return arr;
}