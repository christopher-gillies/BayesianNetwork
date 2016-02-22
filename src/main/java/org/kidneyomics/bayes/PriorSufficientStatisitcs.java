package org.kidneyomics.bayes;

import java.util.Map;

public interface PriorSufficientStatisitcs {
	void addPrior(TableNode node, Map<Row,Double> sufficientStats);
}
