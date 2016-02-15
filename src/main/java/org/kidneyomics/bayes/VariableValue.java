package org.kidneyomics.bayes;

public interface VariableValue<T1 extends Variable,T2 extends Value> {
	T1 variable();
	T2 value();
}
