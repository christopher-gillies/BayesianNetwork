package org.kidneyomics.bayes;

import java.util.Map;

public class UniformPriorSufficientStatistics implements PriorSufficientStatisitcs {
	
	private final double priorBeliefStrength;
	
	private UniformPriorSufficientStatistics(double priorBeliefStrength) {
		this.priorBeliefStrength = priorBeliefStrength;
	}
	
	public static UniformPriorSufficientStatistics create() {
		return new UniformPriorSufficientStatistics(0.1);
	}
			
	public static UniformPriorSufficientStatistics createByBeliefStrength(double priorBeliefStrength) {
		return new UniformPriorSufficientStatistics(priorBeliefStrength);
	}
	
	public void addPrior(TableNode node, Map<Row, Double> sufficientStats) {
		for(Map.Entry<Row, Double> entry : sufficientStats.entrySet()) {
			double val = entry.getValue();
			entry.setValue(val + priorBeliefStrength);
		}
	}

}
