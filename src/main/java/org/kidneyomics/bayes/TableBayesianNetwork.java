package org.kidneyomics.bayes;

import java.util.List;
import java.util.Set;

public interface TableBayesianNetwork extends BayesianNetwork {
	List<TableNode> nodes();
	Set<TableFactor> factors();
	
	TableNode getNode(DiscreteVariable variable);
}