package org.kidneyomics.bayes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TableConditionalProbabilityDistribution implements ProbabilityDistribution {

	private final TableFactor table;
	private final DiscreteVariable unconditionedVariable;
	//private final Map<Row,Double> sufficientStatistics;
	
	private TableConditionalProbabilityDistribution(TableFactor table, DiscreteVariable unconditionedVariable) {
		this.table = table;
		this.unconditionedVariable = unconditionedVariable;
		//this.sufficientStatistics = new HashMap<Row,Double>();
		if(!isNormalized()) {
			normalize();
		}
	}
	
	public TableFactor getFactor() {
		return this.table;
	}
	
	public DiscreteVariable getUnconditionedVariable() {
		return unconditionedVariable;
	}
	
	public static TableConditionalProbabilityDistribution create(TableFactor table, DiscreteVariable unconditionedVariable) {
		return new TableConditionalProbabilityDistribution(table,unconditionedVariable);
	}

	
	public Map<Row,Double> computeSufficientStatisticsCompleteData(List<DiscreteInstance> instances) {
		
		Map<Row,Double> sufficientStatistics = new HashMap<Row,Double>();
		
		//initialize
		for(Row row : table.rows()) {
			sufficientStatistics.put(row, 0.0);
		}
		
		for(DiscreteInstance instance : instances) {
			
			if(instance.containsMissing()) {
				throw new IllegalArgumentException("Error this routine set up for complete data");
			}
			
			// get subset of variables for this node
			List<DiscreteVariableValue> varValsForInstance = instance.subset(table.scope());
			
			boolean used = false;
			//go through each row
			for(Row row : table.rows()) {
				//if the row matches the input then add one to the count
				if(row.hasAllDiscreteVariableValues(varValsForInstance)) {
					double currentVal = sufficientStatistics.get(row);
					
					sufficientStatistics.put(row, currentVal + 1.0);
					if(used == true) {
						throw new IllegalStateException("Error this instance has already been counted for this CPD");
					}
					used = true;
				}
			}
			
			if(used == false) {
				throw new IllegalStateException("Error this was not counted for this CPD");
			}
		}
		
		return sufficientStatistics;
			
	}
	
	public void maximumLikelihoodEstimation(Map<Row,Double> sufficientStatistics) {
		//create row buckets
		HashMap<String,List<Row>> map = createBucketsOfRows();
		
		//normalize
		for(List<Row> rows : map.values()) {
			for(Row row : rows) {
				if(!sufficientStatistics.containsKey(row)) {
					throw new IllegalArgumentException("sufficientStatistics map does not contain row");
				}
				double sufficientStat = sufficientStatistics.get(row);
				
				row.setValue( sufficientStat );
			}
			ProbabilityDistributionUtil.normalize(rows);
		}
	}
	
	/**
	 * 
	 * @return all the rows organized by the conditioned variables
	 */
	private HashMap<String,List<Row>> createBucketsOfRows() {
		HashSet<DiscreteVariable> conditioned = new HashSet<DiscreteVariable>();
		
		conditioned.addAll(table.scope());
		conditioned.remove(unconditionedVariable);
		
		//create row buckets
		HashMap<String,List<Row>> map = new HashMap<String, List<Row>>();
		for(Row row : this.table.rows()) {
			
			StringBuilder sb = new StringBuilder();
			
			Iterator<DiscreteVariable> iter = conditioned.iterator();
			while(iter.hasNext()) {
				DiscreteVariable variable = iter.next();
				DiscreteVariableValue varVal = row.getVariableValue(variable);
				sb.append(varVal.toString());
				if(iter.hasNext()) {
					sb.append(",");
				}
			}
			
			String key = sb.toString();
			
			if(map.containsKey(key)) {
				map.get(key).add(row);
			} else {
				LinkedList<Row> rowsList = new LinkedList<Row>();
				rowsList.add(row);
				map.put(key, rowsList);
			}
			
		}
		
		return map;
	}
	
	public void normalize() {
		
		//create row buckets
		HashMap<String,List<Row>> map = createBucketsOfRows();
		
		//normalize
		for(List<Row> rows : map.values()) {
			ProbabilityDistributionUtil.normalize(rows);
		}
	}

	public boolean isNormalized() {
		//create row buckets
		HashMap<String,List<Row>> map = createBucketsOfRows();
		
		//normalize
		for(List<Row> rows : map.values()) {
			if(!ProbabilityDistributionUtil.isNormalized(rows)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		HashSet<DiscreteVariable> conditioned = new HashSet<DiscreteVariable>();
		
		conditioned.addAll(table.scope());
		conditioned.remove(unconditionedVariable);
		
		//create row buckets
		HashMap<String,List<Row>> map = createBucketsOfRows();
		//normalize
		
		//row key --> column key --> value
		HashMap<String,HashMap<String,Double>> printMap = new HashMap<String, HashMap<String,Double>>();
		
		for(Map.Entry<String, List<Row>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<Row> rows = entry.getValue();
			
			for(Row row : rows) {
				DiscreteVariableValue varVal = row.getVariableValue(unconditionedVariable);
				if(printMap.containsKey(key)) {
					printMap.get(key).put(varVal.getKey(), row.getValue());
				} else {
					HashMap<String,Double> rowMap = new HashMap<String, Double>();
					rowMap.put(varVal.getKey(), row.getValue());
					printMap.put(key, rowMap);
				}
			}
			
		}
		
		
		//loop through the printmap to print the conditional probability table
		List<String> orderOfCols = new LinkedList<String>();
		
		List<String> orderOfRows = new LinkedList<String>();
		orderOfRows.addAll(printMap.keySet());
		Collections.sort(orderOfRows);
		
		for(String rowKey : orderOfRows) {
			HashMap<String,Double> columns = printMap.get(rowKey);
			if(orderOfCols.size() == 0) {
				orderOfCols.addAll(columns.keySet());
				//sort
				Collections.sort(orderOfCols);
				
				//print header
				
				//no value in first column
				sb.append("\t");
				Iterator<String> iter = orderOfCols.iterator();
				while(iter.hasNext()) {
					sb.append(iter.next());
					if(iter.hasNext()) {
						sb.append("\t");
					}
				}
				sb.append("\n");
			}
			
			//write row key
			sb.append(rowKey);
			sb.append("\t");
			//write column values
			Iterator<String> iter = orderOfCols.iterator();
			while(iter.hasNext()) {
				sb.append(columns.get(iter.next()));
				if(iter.hasNext()) {
					sb.append("\t");
				}
			}
			sb.append("\n");
		
		}
		
		return sb.toString();
	}
	
}
