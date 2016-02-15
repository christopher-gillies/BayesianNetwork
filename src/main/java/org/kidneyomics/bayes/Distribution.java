package org.kidneyomics.bayes;

public interface Distribution {
	void normalize();
	boolean isNormalized();
}
