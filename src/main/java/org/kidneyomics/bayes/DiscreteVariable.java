package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscreteVariable implements Variable {
	private final String name;
	private final Map<String,DiscreteValue> values;
	private final Map<String,DiscreteVariableValue> variableValues;
	
	private DiscreteVariable(String name, Set<DiscreteValue> values) {
		this.name = name;
		this.values = new HashMap<String,DiscreteValue>();
		this.variableValues = new HashMap<String, DiscreteVariableValue>();
		for(DiscreteValue value : values) {
			if(this.values.containsKey(value.getName())) {
				throw new RuntimeException("Duplicate name error");
			}
			this.values.put(value.getName(), value);
			
			this.variableValues.put(value.getName(), DiscreteVariableValue.create(this, value));
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
	
	public boolean hasValue(String name) {
		return this.values.containsKey(name);
	}
	
	
	public DiscreteValue getValueByName(String name) {
		return this.values.get(name);
	}
	
	public DiscreteVariableValue getVariableValueByName(String name) {
		return this.variableValues.get(name);
	}
	
	public Collection<DiscreteValue> values() {
		return this.values.values();
	}
	
	public List<DiscreteValue> valuesSorted() {
		List<DiscreteValue> valuesArray = new ArrayList<DiscreteValue>(this.values().size());
		valuesArray.addAll(this.values());
		Collections.sort(valuesArray);
		return valuesArray;
	}
	
	
	@Override
	public String toString() {
		return this.name;
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
