package org.kidneyomics.bayes;

public class DiscreteVariableValue implements VariableValue {
	
	private final DiscreteVariable variable;
	private final DiscreteValue value;
	private final String key;
	
	private DiscreteVariableValue(DiscreteVariable variable, DiscreteValue value) {
		this.value = value;
		this.variable = variable;
		this.key = variable.getName() + "=" + value.getValue();
	}
	
	public static DiscreteVariableValue create(DiscreteVariable variable, DiscreteValue value) {
		DiscreteVariableValue varval = new DiscreteVariableValue(variable,value);

		return varval;
	}
	
	public Variable variable() {
		return this.variable;
	}

	public Value value() {
		return this.value;
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean equals(Object o) {
		if(o instanceof DiscreteVariableValue) {
			DiscreteVariableValue other = (DiscreteVariableValue) o;
			return this.getKey().equals(other.getKey());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
	    hash = hash * 31 + key.hashCode();
	    return hash;
	}
}
