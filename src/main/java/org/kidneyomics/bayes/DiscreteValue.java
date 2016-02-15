package org.kidneyomics.bayes;

public class DiscreteValue implements Value {
	
	private final String name;
	
	private DiscreteValue(String value) {
		this.name = value;
	}
	
	public static DiscreteValue create(String value) {
		DiscreteValue discreteValue = new DiscreteValue(value);	
		return discreteValue;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
