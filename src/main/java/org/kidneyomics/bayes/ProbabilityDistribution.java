package org.kidneyomics.bayes;

public interface ProbabilityDistribution {
	void normalize();
	boolean isNormalized();
}
