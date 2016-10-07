#include "oarlib_graph_util_BlossomV.h"
#include "PerfectMatching.h"

JNIEXPORT jintArray JNICALL Java_oarlib_graph_util_BlossomV_blossomV
  (JNIEnv *env, jclass mclass, jint node_num, jint edge_num, jintArray edges, jintArray weights) {

  	struct PerfectMatching::Options options;

  	jintArray ret = env->NewIntArray(node_num);
  	jint *retContents = env->GetIntArrayElements(ret, NULL);
  	jint *edgesContents = env->GetIntArrayElements(edges, NULL);
  	jint *weightsContents = env->GetIntArrayElements(weights,NULL);

  	PerfectMatching *pm = new PerfectMatching(node_num, edge_num);
  	int e, i, j;
	for (e=0; e<edge_num; e++) pm->AddEdge(edgesContents[2*e], edgesContents[2*e+1], weightsContents[e]);
	pm->options = options;
	pm->Solve();

	for (i=0; i<node_num; i++)
	{
		j = pm->GetMatch(i);
		retContents[i] = j;
	}

	env->ReleaseIntArrayElements(ret,retContents,NULL);
	env->ReleaseIntArrayElements(edges,edgesContents,NULL);
	env->ReleaseIntArrayElements(weights,weightsContents,NULL);
	return ret;
	
  }

  int main(){
  	return 0;
  }

