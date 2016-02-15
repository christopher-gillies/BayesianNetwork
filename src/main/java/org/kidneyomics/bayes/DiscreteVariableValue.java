package org.kidneyomics.bayes;

public class DiscreteVariableValue implements VariableValue<DiscreteVariable,DiscreteValue>, Comparable<DiscreteVariableValue> {
	
	private final DiscreteVariable variable;
	private final DiscreteValue value;
	private final String key;
	
	private DiscreteVariableValue(DiscreteVariable variable, DiscreteValue value) {
		this.value = value;
		this.variable = variable;
		this.key = variable.getName() + "=" + value.getName();
	}
	
	public static DiscreteVariableValue create(DiscreteVariable variable, DiscreteValue value) {
		DiscreteVariableValue varval = new DiscreteVariableValue(variable,value);

		return varval;
	}
	
	public DiscreteVariable variable() {
		return this.variable;
	}

	public DiscreteValue value() {
		return this.value;
	}
	
	public String getKey() {
		return key;
	}
	
	@Override
	public String toString() {
		return getKey();
	}
	
	public boolean equals(Object o) {
		if(o instanceof DiscreteVariableValue) {
			DiscreteVariableValue other = (DiscreteVariableValue) o;
			return this.getKey().equals(other.getKey());
		} else {
			return false;
		}
	}
	
	//key is the basis for the hashcode
	@Override
	public int hashCode() {
		int hash = 1;
	    hash = hash * 31 + key.hashCode();
	    return hash;
	}

	
	//compare keys
	public int compareTo(DiscreteVariableValue o) {
		DiscreteVariableValue varVal = (DiscreteVariableValue) o;
		return this.key.compareTo(varVal.getKey());
	}
}
