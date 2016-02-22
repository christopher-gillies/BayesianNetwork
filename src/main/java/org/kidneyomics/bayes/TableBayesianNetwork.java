package org.kidneyomics.bayes;

import java.util.List;
import java.util.Set;

public interface TableBayesianNetwork extends BayesianNetwork {
	String name();
	List<TableNode> nodes();
	
	/**
	 * 
	 * @return a copy of the factors for the table
	 */
	Set<TableFactor> factors();
	Set<DiscreteVariable> variables();
	
	TableNode getNode(DiscreteVariable variable);
	
	DiscreteVariable getVariableByName(String name);
}
