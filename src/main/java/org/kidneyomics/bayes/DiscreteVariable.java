package org.kidneyomics.bayes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiscreteVariable implements Variable {
	private final String name;
	private final Map<String,DiscreteValue> values;
	
	private DiscreteVariable(String name, Set<DiscreteValue> values) {
		this.name = name;
		this.values = new HashMap<String,DiscreteValue>();
		for(DiscreteValue value : values) {
			if(this.values.containsKey(value.getName())) {
				throw new RuntimeException("Duplicate name error");
			}
			this.values.put(value.getName(), value);
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param discrete values each value should have its own unique name 
	 * @return
	 */
	public static DiscreteVariable create(String name, Set<DiscreteValue> values) {
		return new DiscreteVariable(name,values);
	}
	
	public static DiscreteVariable create(String name, DiscreteValue... values) {
		HashSet<DiscreteValue> valuesSet = new HashSet<DiscreteValue>();
		for(DiscreteValue value : values) {
			valuesSet.add(value);
		}
		return create(name,valuesSet);
	}
	
	public String getName() {
		return name;
	}
	
	public DiscreteValue getValueByName(String name) {
		return this.values.get(name);
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof DiscreteVariable) {
			DiscreteVariable other = (DiscreteVariable) o;
			return this.getName().equals(other.getName());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return 31 * this.getName().hashCode();
	}
}
