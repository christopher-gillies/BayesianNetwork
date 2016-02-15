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
	
	private TableFactor(Set<DiscreteVariable> variables) {
		scope = new HashMap<String,DiscreteVariable>();
		
		for(DiscreteVariable variable : variables) {
			if(scope.containsKey(variable.getName())) {
				throw new RuntimeException("Table already has variable with this name");
			}
			
			scope.put(variable.getName(), variable);
		}
		
		this.rows = new LinkedList<Row>();
	}
	
	public static TableFactor create(Set<DiscreteVariable> variables) {
		TableFactor factor = new TableFactor(variables);
		return factor;
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
	
	public DiscreteVariable getVariableByName(String name) {
		return scope.get(name);
	}

	public Collection<DiscreteVariable> scope() {
		return scope.values();
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
		for(Row row : this.rows) {
			Row possibleNewRow = row.createRowFromVariableSubset(varSet, true);
			if(rowMap.containsKey(possibleNewRow.key())) {
				Row newRow = rowMap.get(possibleNewRow.key());
				newRow.addToValue(possibleNewRow.getValue());
			} else {
				rowMap.put(possibleNewRow.key(), possibleNewRow);
			}
		}
		
		//store results
		for(Row row : rowMap.values()) {
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
		
		if(intersection.size() == 0) {
			throw new IllegalArgumentException("The factors must overlap by some variable");
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
	 * @param clone -- true or false specifying whether or not the rows should be cloneed or not
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

}
