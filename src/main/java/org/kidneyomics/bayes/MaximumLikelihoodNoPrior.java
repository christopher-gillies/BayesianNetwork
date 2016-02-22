package org.kidneyomics.bayes;

import java.util.Map;

public class MaximumLikelihoodNoPrior implements PriorSufficientStatisitcs {

	public static MaximumLikelihoodNoPrior create() {
		return new MaximumLikelihoodNoPrior();
	}
	
	private MaximumLikelihoodNoPrior() {
		
	}
	/**
	 * Do nothing
	 */
	public void addPrior(TableNode node, Map<Row, Double> sufficientStats) {
		return;
	}

}
