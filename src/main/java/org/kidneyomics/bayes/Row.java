package org.kidneyomics.bayes;

import java.util.HashSet;
import java.util.Set;

public class Row {

	private final Set<DiscreteVariableValue> variableValues;
	private double value;
	
	private Row(Set<DiscreteVariableValue> variableValues) {
		this.variableValues = new HashSet<DiscreteVariableValue>();
		this.variableValues.addAll(variableValues);
	}
	
	public Row getRow(Set<DiscreteVariableValue> variableValues) {
		Row row = new Row(variableValues);
		
		return row;
	}
	
	public boolean hasDiscreteVariableValue(DiscreteVariableValue varVal) {
		return this.variableValues.contains(varVal);
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public void addToValue(double value) {
		this.value = this.value + value;
	}
	
	//public String getKey() {
	//	return this.key;
	//}
}
