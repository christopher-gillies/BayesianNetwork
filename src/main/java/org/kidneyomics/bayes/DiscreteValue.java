package org.kidneyomics.bayes;

public class DiscreteValue implements Value {
	
	private final String value;
	
	private DiscreteValue(String value) {
		this.value = value;
	}
	
	public static DiscreteValue create(String value) {
		DiscreteValue discreteValue = new DiscreteValue(value);	
		return discreteValue;
	}
	
	String getValue() {
		return this.value;
	}
}
