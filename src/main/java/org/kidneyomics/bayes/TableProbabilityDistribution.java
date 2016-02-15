package org.kidneyomics.bayes;

public class TableProbabilityDistribution implements ProbabilityDistribution {

	private final TableFactor table;
	
	private TableProbabilityDistribution(TableFactor table) {
		this.table = table;
		if(!isNormalized()) {
			normalize();
		}
	}
	
	public static TableProbabilityDistribution create(TableFactor table) {
		return new TableProbabilityDistribution(table);
	}
	

	public void normalize() {
		ProbabilityDistributionUtil.normalize(table);
	}
	

	public TableFactor getFactor() {
		return this.table;
	}
	
	public boolean isNormalized() {
		return ProbabilityDistributionUtil.isNormalized(table);
	}
	
	@Override
	public String toString() {
		return this.table.toString();
	}
	


}
