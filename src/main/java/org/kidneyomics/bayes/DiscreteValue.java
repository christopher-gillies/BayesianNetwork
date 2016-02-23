package org.kidneyomics.bayes;

public class DiscreteValue implements Value, Comparable<DiscreteValue> {
	
	private final String name;
	private final boolean isMissing;
	
	private DiscreteValue(String value, boolean isMissing) {
		this.name = value;
		this.isMissing = isMissing;
	}
	
	public static DiscreteValue create(String value) {
		DiscreteValue discreteValue = new DiscreteValue(value,false);	
		return discreteValue;
	}
	
	public static DiscreteValue createMissing() {
		DiscreteValue discreteValue = new DiscreteValue("NA",true);	
		return discreteValue;
	}
	
	public boolean isMissing() {
		return isMissing;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(DiscreteValue o) {
		return this.getName().compareTo(o.getName());
	}
	
}
