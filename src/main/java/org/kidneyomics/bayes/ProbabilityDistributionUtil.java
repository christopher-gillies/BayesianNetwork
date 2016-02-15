package org.kidneyomics.bayes;

import java.util.Collection;

public class ProbabilityDistributionUtil {

	public static boolean isNormalized(TableFactor table) {
		double sum = sum(table.rows());
		
		return Math.abs(sum - 1.0) <= Math.pow(10.0, -6);
	}
	
	public static boolean isNormalized(Collection<Row> rows) {
		double sum = sum(rows);
		
		return Math.abs(sum - 1.0) <= Math.pow(10.0, -6);
	}
	
	
	public static void normalize(TableFactor table) {
		normalize(table.rows());
	}
	
	
	public static double sum(Collection<Row> rows) {
		double sum = 0.0;
		for(Row row : rows) {
			sum += row.getValue();
		}
		return sum;
	}
	
	public static void normalize(Collection<Row> rows) {
		double sum = sum(rows);
		
		for(Row row : rows) {
			double val =  row.getValue();
			row.setValue(val / sum);
		}
	}
	
}
