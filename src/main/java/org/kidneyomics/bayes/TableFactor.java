package org.kidneyomics.bayes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableFactor implements Factor<DiscreteVariable,DiscreteVariableValue> {

	private final Map<String,DiscreteVariable> scope;
	private final List<Row> rows;
	private final Set<DiscreteVariable> scopeSet;
	
	private TableFactor(Set<DiscreteVariable> variables) {
		scope = new HashMap<String,DiscreteVariable>();
		scopeSet = new HashSet<DiscreteVariable>();
		
		for(DiscreteVariable variable : variables) {
			if(scope.containsKey(variable.getName())) {
				throw new RuntimeException("Table already has variable with this name");
			}
			
			scope.put(variable.getName(), variable);
			scopeSet.add(variable);
		}
		
		this.rows = new LinkedList<Row>();
	}
	
	/**
	 * 
	 * @param scope -- the variables included in the table
	 * @return TableFactor set up for these variables
	 * 
	 */
	public static TableFactor create(Set<DiscreteVariable> scope) {
		TableFactor factor = new TableFactor(scope);
		return factor;
	}
	
	/**
	 * 
	 * @param scope -- the variables included in the table
	 * @return TableFactor set up for these variables
	 * 
	 */
	public static TableFactor create(DiscreteVariable... scope) {
		Set<DiscreteVariable> scopeSet = new HashSet<DiscreteVariable>();
		for(DiscreteVariable variable : scope) {
			scopeSet.add(variable);
		}
		return create(scopeSet);
	}
		
	public TableFactor addRow(Row row) {
		this.rows.add(row);
		return this;
	}
	
	public TableFactor addRows(Row... rows) {
		for(Row row : rows) {
			addRow(row);
		}
		return this;
	}
	
	public List<Row> rows() {
		return this.rows;
	}
	
	/**
	 * 
	 * @param variable -- the variable to get the DiscreteValues for
	 * @return the set of all values for Variable in this factor
	 */
	public Set<DiscreteValue> getVariableValuesByVariable(DiscreteVariable variable) {
		HashSet<DiscreteValue> values = new HashSet<DiscreteValue>();
		
		for(Row row : rows) {
			values.add(row.getVariableValue(variable).value());
		}
		
		return values;
	}
	
	public DiscreteVariable getVariableByName(String name) {
		return scope.get(name);
	}

	public Set<DiscreteVariable> scope() {
		return scopeSet;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		Iterator<DiscreteVariable> iter = scope().iterator();
		while(iter.hasNext()) {
			DiscreteVariable next = iter.next();
			sb.append(next.getName());
			
			//if(iter.hasNext()) {
				sb.append("\t");
			//}
		}
		
		sb.append("Value");
		
		
		sb.append("\n");
		
		for(Row row : rows) {
			sb.append(row.toString(scope()));
			sb.append("\n");
		}
		
		
		return sb.toString();
		
	}
	
	public Factor<DiscreteVariable, DiscreteVariableValue> marinalize(DiscreteVariable... variables) {
		HashSet<DiscreteVariable> variablesSet = new HashSet<DiscreteVariable>();
		for(DiscreteVariable variable : variables) {
			variablesSet.add(variable);
		}
		return marginalize(variablesSet);
	}
	
	public Factor<DiscreteVariable, DiscreteVariableValue> marginalize(Set<DiscreteVariable> variables) {
		//marginalize a single variable
		HashSet<DiscreteVariable> varSet = new HashSet<DiscreteVariable>();
		varSet.addAll(this.scope.values());
		
		for(DiscreteVariable variable : variables) {
			if(!varSet.contains(variable)) {
				throw new RuntimeException("Cannot marginalize " + variable + " because this variable is not found in the set for this factor");
			}
			
			//remove variable
			varSet.remove(variable);
		}
		
		TableFactor newFactor = TableFactor.create(varSet);

		//create new marginalized rows
		HashMap<String,Row> rowMap = new HashMap<String, Row>();	
		
		//store list of row order for visualization
		List<Row> rowOrder = new LinkedList<Row>();
		for(Row row : this.rows) {
			Row possibleNewRow = row.createRowFromVariableSubset(varSet, true);
			if(rowMap.containsKey(possibleNewRow.key())) {
				Row newRow = rowMap.get(possibleNewRow.key());
				newRow.addToValue(possibleNewRow.getValue());
			} else {
				rowMap.put(possibleNewRow.key(), possibleNewRow);
				rowOrder.add(possibleNewRow);
			}
		}
		
		//store results
		for(Row row : rowOrder) {
			newFactor.addRow(row);
		}
		
		return newFactor;
	}

	public Factor<DiscreteVariable, DiscreteVariableValue> product(
			Factor<DiscreteVariable, DiscreteVariableValue> factor) {
		
		TableFactor tFactor = (TableFactor) factor;
	
		
		Set<DiscreteVariable> newVariableSet = new HashSet<DiscreteVariable>();
		newVariableSet.addAll(this.scope());
		
		//find overlap and store new scope
		Set<DiscreteVariable> intersection = new HashSet<DiscreteVariable>();
		for(String key : tFactor.scope.keySet()) {
			if(this.scope.containsKey(key)) {
				intersection.add(this.getVariableByName(key));
			} else {
				newVariableSet.add(tFactor.getVariableByName(key));
			}
		}
		
		
		//Create result factor
		TableFactor result = TableFactor.create(newVariableSet);
		

		for(Row rowA : this.rows) {
			for(Row rowB : tFactor.rows) {	
				if(rowA.compatible(rowB, intersection)) {
					Row resRow = rowA.product(rowB, newVariableSet);
					result.addRow(resRow);
				}
			}
		}
		
		
		return result;
		
	}

	public Factor<DiscreteVariable, DiscreteVariableValue> reduce(DiscreteVariableValue... discreteVariableValues) {
		HashSet<DiscreteVariableValue> set = new HashSet<DiscreteVariableValue>();
		for(DiscreteVariableValue var : discreteVariableValues) {
			set.add(var);
		}
		return reduce(set);
	}
	
	public Factor<DiscreteVariable, DiscreteVariableValue> reduce(Set<DiscreteVariableValue> variableValues) {
		//this operation is the factor equivalent of conditioning
		
		HashSet<DiscreteVariable> set = new HashSet<DiscreteVariable>();
		set.addAll(this.scope.values());
		
		TableFactor newFactor = TableFactor.create(set);
		
		List<Row> reducedRows = this.getRowsByValues(true, variableValues);
		
		for(Row row : reducedRows) {
			newFactor.addRow(row);
		}
		
		
		return newFactor;
	}
	
	/**
	 * 
	 * @param variableValues
	 * @param clone -- true or false specifying whether or not the rows should be cloned or not
	 * @return a list of rows meeting the criteria input
	 */
	public List<Row> getRowsByValues(boolean clone, Set<DiscreteVariableValue> variableValues) {
		List<Row> rowsRes = new LinkedList<Row>();
		
		//clone the rows or not
		if(clone) {
			for(Row row : this.rows) {
				if(row.hasAllDiscreteVariableValues(variableValues)) {
					rowsRes.add( (Row) row.clone() );
				}
			}
		} else {
			for(Row row : this.rows) {
				if(row.hasAllDiscreteVariableValues(variableValues)) {
					rowsRes.add( row );
				}
			}
		}
		
		return rowsRes;
	}
	
	/**
	 * 
	 * @param variableValues
	 * @param clone -- true or false specifying whether or not the rows should be cloneed or not
	 * @return a list of rows meeting the criteria input
	 */
	public List<Row> getRowsByValues(boolean clone, DiscreteVariableValue... variableValues) {
		HashSet<DiscreteVariableValue> result = new HashSet<DiscreteVariableValue>();
		for(DiscreteVariableValue varVal : variableValues) {
			result.add(varVal);
		}
		return getRowsByValues(clone,result);
	}
	
	/**
	 * 
	 * @param clone
	 * @param variableValues -- the row key
	 * @return -- find the first row matching this key
	 */
	public Row getRowByValues(boolean clone, DiscreteVariableValue... variableValues) {
		List<Row> result = getRowsByValues(clone,variableValues);
		if(result.size() > 0) {
			return getRowsByValues(clone,variableValues).get(0);
		} else {
			return null;
		}
	}

}
