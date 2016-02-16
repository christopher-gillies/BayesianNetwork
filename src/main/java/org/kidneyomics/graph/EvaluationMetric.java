package org.kidneyomics.graph;

public interface EvaluationMetric<T> {
	double calculate(UndirectedNode<T> node);
}
