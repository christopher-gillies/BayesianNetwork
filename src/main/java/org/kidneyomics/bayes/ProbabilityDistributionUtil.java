package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kidneyomics.random.DoubleDescendingComparator;

public class ProbabilityDistributionUtil {

	private static DoubleDescendingComparator comparator = new DoubleDescendingComparator();
	
	public static boolean isNormalized(TableFactor table) {
		return isNormalized(table.rows());
	}
	
	public static boolean isNormalized(Collection<Row> rows) {
		double sum = Math.exp(sumLogScale(rows));
		
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
	
	public static double sumLogScale(Collection<Row> rows) {
		LinkedList<Double> logs = new LinkedList<Double>();
		for(Row row : rows) {
			logs.add(row.getLogValue());
		}
		
		return computeLogOfSumLogs(logs);
	}
	
	
	
	public static void normalize(Collection<Row> rows) {

		
		double logSum = sumLogScale(rows);
		
		for(Row row : rows) {
			double logVal =  row.getLogValue();
			row.setLogValue(logVal - logSum);
		}
	}
	
	public static double addLogValues(double logA, double logB) {
		if(logA < logB) {
			double tmp = logA;
			logA = logB;
			logB = tmp;
		}
		
		return logA + Math.log(1 + Math.exp( logB - logA));
	}
	
	static double computeLogOfSum(List<Double> vals) {
		List<Double> logs = new ArrayList<Double>(vals.size());
		for(Double val : vals) {
			logs.add(Math.log(val));
		}
		
		double res = computeLogOfSumLogs(logs);
		return res;
	}
	
	static double computeLogOfSumLogs(List<Double> logs) {
		
		// log sum (a_i) = log a_0 + log( 1 + sum( exp( log(a_i) - log(a_0)))
		
		/*
		 * sort in descending order
		 */
		
		Collections.sort(logs, comparator);
		
		Iterator<Double> iter = logs.iterator();
		
		double largest = iter.next();
		double sum = 0;
		while(iter.hasNext()) {
			double next = iter.next();
			sum = sum + Math.exp(next - largest);
		}
		
		return largest + Math.log(1 + sum);
		
	}
	
}
