package org.kidneyomics.bayes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TableFactor implements Factor {

	private final Set<DiscreteVariable> scope;
	private final List<Row> rows;
	
	private TableFactor(Set<DiscreteVariable> variables) {
		scope = new HashSet<DiscreteVariable>();
		scope.addAll(variables);
		this.rows = new LinkedList<Row>();
	}
	
	public static TableFactor create(Set<DiscreteVariable> variables) {
		TableFactor factor = new TableFactor(variables);
		
		
		
		return factor;
	}
	
	public Set<Variable> scope() {
		// TODO Auto-generated method stub
		return null;
	}

	public Factor marinalize(Set<Variable> variables) {
		// TODO Auto-generated method stub
		return null;
	}

	public Factor product(Factor factor) {
		// TODO Auto-generated method stub
		return null;
	}

	public Factor reduce(Set<VariableValue> variableValues) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addRow(Row row) {
		this.rows.add(row);
	}

}
