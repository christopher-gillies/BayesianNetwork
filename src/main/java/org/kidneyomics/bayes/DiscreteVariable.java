package org.kidneyomics.bayes;

import java.util.HashSet;
import java.util.Set;

public class DiscreteVariable implements Variable {
	private final String name;
	private final Set<DiscreteValue> values;
	
	private DiscreteVariable(String name, Set<DiscreteValue> values) {
		this.name = name;
		this.values = new HashSet<DiscreteValue>();
		this.values.addAll(values);
	}
	
	public static DiscreteVariable create(String name, Set<DiscreteValue> values) {
		return new DiscreteVariable(name,values);
	}
	
	public String getName() {
		return name;
	}
}
