package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Row implements Cloneable {

	private final Map<DiscreteVariable,DiscreteVariableValue> variableValuesMap;
	private final Set<DiscreteVariableValue> variableValuesSet;
	private final String key;
	
	private double logValue;
	
	
	private Row(double logValue, Collection<DiscreteVariableValue> variableValues) {
		this.variableValuesSet = new HashSet<DiscreteVariableValue>();
		variableValuesSet.addAll(variableValues);
		this.variableValuesMap = new HashMap<DiscreteVariable,DiscreteVariableValue>();
		for(DiscreteVariableValue varVal : variableValues) {
			this.variableValuesMap.put(varVal.variable(), varVal);
		}
		this.logValue = logValue;
		
		//Create row key
		StringBuilder sb = new StringBuilder();
		//create sorted list to ensure order
		List<DiscreteVariableValue> variableValList = new ArrayList<DiscreteVariableValue>(variableValuesSet.size());
		variableValList.addAll(variableValuesSet);
		Collections.sort(variableValList);
		
		Iterator<DiscreteVariableValue> iter = variableValList.iterator();
		while(iter.hasNext()) {
			DiscreteVariableValue varVal = iter.next();
			sb.append(varVal.toString());
			if(iter.hasNext()) {
				sb.append("\t");
			}
		}
		key = sb.toString();
	}
	
	/**
	 * Create a row from variable values
	 * @param value -- a value to store in the row. Assumes it is not log scaled
	 * @param variableValues -- these define the random variable values that correspond to the value for this row. e.g. (grade = g1, letter = l0) = 0.1
	 * @return a row instance
	 */
	public static Row create(double value, Collection<DiscreteVariableValue> variableValues) {
		return Row.create(false, value, variableValues);
	}
	
	/**
	 * 
	 * @param isLogScaled is the value log scaled
	 * @param value -- value in log scale
	 * @param variableValues -- row identifier
	 * @return
	 */
	public static Row create(boolean isLogScaled, double value, Collection<DiscreteVariableValue> variableValues) {
		if(!isLogScaled) {
			value = Math.log(value);
		}
		Row row = new Row(value, variableValues);
		return row;
	}
	
	
	public static Row create(double value, DiscreteVariableValue... varVals) {
		return Row.create(false, value, varVals);
	}
	
	/**
	 * 
	 * @param isLogScaled is the value log scaled
	 * @param value -- value in log scale
	 * @param variableValues -- row identifier
	 * @return
	 */
	public static Row create(boolean isLogScaled, double value, DiscreteVariableValue... varVals ) {
		if(!isLogScaled) {
			value = Math.log(value);
		}
		List<DiscreteVariableValue> list = Arrays.asList(varVals);
		Row row = new Row(value, list);
		return row;
	}
	
	public String key() {
		return this.key;
	}
	
	public boolean hasDiscreteVariable(DiscreteVariable var) {
		return this.variableValuesMap.keySet().contains(var);
	}
	
	public boolean hasAllDiscreteVariables(Collection<DiscreteVariable> vars) {
		return this.variableValuesMap.keySet().containsAll(vars);
	}
	
	public boolean hasDiscreteVariableValue(DiscreteVariableValue varVal) {
		return this.variableValuesSet.contains(varVal);
	}
	
	public boolean hasAllDiscreteVariableValues(Collection<DiscreteVariableValue> varVals) {
		return this.variableValuesSet.containsAll(varVals);
	}
	
	public Collection<DiscreteVariableValue> variableValueSet() {
		return this.variableValuesSet;
	}
	
	public DiscreteVariableValue getVariableValue(DiscreteVariable variable) {
		return variableValuesMap.get(variable);
	}
	
	public double getValue() {
		return Math.exp(logValue);
	}
	
	public double getLogValue() {
		return logValue;
	}
	
	public void setValue(double value) {
		this.logValue = Math.log(value);
	}
	
	public void setLogValue(double logValue) {
		this.logValue = logValue;
	}
	
	public void addToValue(double value) {
		if(this.logValue == Double.NEGATIVE_INFINITY) {
			this.logValue = Math.log(value);
		} else {
			this.logValue = ProbabilityDistributionUtil.addLogValues(this.logValue, Math.log(value));
		}
	}
	
	
	public void addToLogValue(double logValueToAdd) {
		if(this.logValue == Double.NEGATIVE_INFINITY) {
			this.logValue = logValueToAdd;
		} else {
			this.logValue = ProbabilityDistributionUtil.addLogValues(this.logValue, logValueToAdd);
		}
	}
	
	@Override
	public String toString() {
		return toString(this.variableValuesMap.keySet());
	}
	
	/**
	 * 
	 * @param subset of variables to include in row
	 * @param copyValue if true put the value of this row in otherwise put 0
	 * @return 
	 */
	public Row createRowFromVariableSubset(Set<DiscreteVariable> subset, boolean copyValue) {
		List<DiscreteVariableValue> varVals = new LinkedList<DiscreteVariableValue>();
		for(DiscreteVariable variable : subset) {
			DiscreteVariableValue varVal = this.getVariableValue(variable);
			varVals.add(varVal);
		}
		if(copyValue) {
			return Row.create(true, this.logValue, varVals);
		} else {
			return Row.create(0.0, varVals);
		}
	}
	
	public String toString(Collection<DiscreteVariable> order) {
		StringBuilder sb = new StringBuilder();
		
		// do not print header just the values
		
		Iterator<DiscreteVariable> iter = order.iterator();
		while(iter.hasNext()) {
			DiscreteVariable next = iter.next();
			
			sb.append(this.variableValuesMap.get(next).value());
			
			//if(iter.hasNext()) {
				sb.append("\t");
			//}
		}
		
		sb.append(this.getValue());
		
		
		return sb.toString();
	}
	
	@Override
	public Object clone() {
		//Allows for a new value to be set for the row without modifying the old row
		Row newRow = Row.create(true, getLogValue(), this.variableValuesSet);
		return newRow;
	}
	
	/**
	 * 
	 * @param other -- row to check compatibility for product
	 * @return true if rows have overlapping columns and same value in those columns
	 */
	public boolean compatible(Row other) {
		Set<DiscreteVariable> varsA = this.variableValuesMap.keySet();
		Set<DiscreteVariable> varsB = other.variableValuesMap.keySet();
		
		HashSet<DiscreteVariable> intersection = new HashSet<DiscreteVariable>();
		
		for(DiscreteVariable variable : varsA) {
			if(varsB.contains(variable)) {
				intersection.add(variable);
			}
		}
		
		return compatible(other, intersection);
	}
	/**
	 * 
	 * @param other -- row to compare with
	 * @param intersectingVariables --intersecting variables for both rows
	 * @return true if rows have overlapping columns and same value in those columns or if there is no overlap at all
	 */
	public boolean compatible(Row other, Set<DiscreteVariable> intersectingVariables) {
		
		for(DiscreteVariable variable : intersectingVariables) {
			DiscreteVariableValue varValA = this.getVariableValue(variable);
			DiscreteVariableValue varValB = other.getVariableValue(variable);
			
			if(varValA == null || varValB == null) {
				throw new IllegalArgumentException("Neither row contians " + variable);
			} else if(varValA.value().equals(varValB.value())) {
				continue;
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * 
	 * @param other -- row to compute produce with
	 * @param newScope -- the scope of the new factor
	 * @return
	 */
	public Row product(Row other, Set<DiscreteVariable> newScope) {
		
		double logResult = this.getLogValue() + other.getLogValue();
		
		//create new merged variable values
		Set<DiscreteVariableValue> varVals = new HashSet<DiscreteVariableValue>();
		
		for(DiscreteVariable variable : newScope) {
			DiscreteVariableValue varValA = this.getVariableValue(variable);
			DiscreteVariableValue varValB = other.getVariableValue(variable);
			
			if(varValA == null && varValB != null) {
				varVals.add(varValB);
			} else if(varValB == null && varValA != null) {
				varVals.add(varValA);
			} else if(varValA == null && varValB == null) {
				throw new IllegalArgumentException("Variable " + variable + " not in either row");
			} else if(varValA.value().equals(varValB.value())) {
				varVals.add(varValA);
			} else {
				throw new IllegalArgumentException("Incompatible rows");
			}
		}
		
		return Row.create(true, logResult, varVals);
	}
}
