package org.kidneyomics.bayes;

public class TableDistribution implements Distribution {

	private final TableFactor table;
	
	private TableDistribution(TableFactor table) {
		this.table = table;
		if(!isNormalized()) {
			normalize();
		}
	}
	
	public static TableDistribution create(TableFactor table) {
		return new TableDistribution(table);
	}
	
	public void normalize() {
		double sum = 0.0;
		for(Row row : table.rows()) {
			sum += row.getValue();
		}
		
		for(Row row : table.rows()) {
			double val =  row.getValue();
			row.setValue(val / sum);
		}
	}

	public TableFactor getTable() {
		return this.table;
	}
	
	public boolean isNormalized() {
		return isNormalized(table);
	}
	
	public static boolean isNormalized(TableFactor table) {
		double sum = 0.0;
		for(Row row : table.rows()) {
			sum += row.getValue();
		}
		
		return Math.abs(sum - 1.0) <= Math.pow(10.0, -6);
	}

}
