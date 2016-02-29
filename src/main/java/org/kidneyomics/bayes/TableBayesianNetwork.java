package org.kidneyomics.bayes;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.kidneyomics.bayes.json.JSON_TableBayesianNetwork;

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
	
	JSON_TableBayesianNetwork toJSON();
	
	List<DiscreteInstance> forwardSample(int n);
	
	void learnFromCompleteData(List<DiscreteInstance> instances);
	void learnFromMissingData(List<DiscreteInstance> instances);
}
