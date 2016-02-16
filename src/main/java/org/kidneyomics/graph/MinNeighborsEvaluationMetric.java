package org.kidneyomics.graph;

public class MinNeighborsEvaluationMetric<T> implements EvaluationMetric<T> {

	public double calculate(UndirectedNode<T> node) {
		return node.neighbors().size();
	}

}
