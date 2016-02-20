package org.kidneyomics.bayes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kidneyomics.bayes.CliqueTree.CliqueNode;

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
	
	
	public Map<Row,Double> computeSufficientStatisticsMissingData(Collection<CliqueTree> trees) {
		
		Map<Row,Double> sufficientStatistics = new HashMap<Row,Double>();
		
		//initialize
		for(Row row : table.rows()) {
			sufficientStatistics.put(row, 0.0);
		}
		
		for(CliqueTree tree : trees) {
			//go through each row
			double totalContribution = 0;
			//get clique tree node for this table CPD
			CliqueNode node = tree.getSmallestNodeByScope(this.table.scope());
			if(node == null) {
				throw new IllegalStateException("No clique node found with this table's scope");
			}
			//belief table scope should be at least as big as this table's scope? right?
			TableFactor belief = node.belief();
			if(belief == null || belief.rows().size() == 0) {
				throw new IllegalStateException("No probabilty for this sample");
			}
			//normalize the distribution results
			TableProbabilityDistribution beliefDist = TableProbabilityDistribution.create(belief);
			for(Row beliefRow : beliefDist.getFactor().rows()) {
				beliefRow = beliefRow.createRowFromVariableSubset(this.table.scope(), true);
				double probability = beliefRow.getValue();
				Row tableRow = this.table.getRowByValues(false, beliefRow.variableValueSet());
				double currentVal = sufficientStatistics.get(tableRow);
				sufficientStatistics.put(tableRow, currentVal + probability);
				//store contribution
				totalContribution += probability;
			}
			//add contribution to rows for every value of belief factor
			//for(Row row : table.rows()) {

				
				//if the row matches the input then add one to the count
				//TODO: only select rows that match
				//if(row.hasAllDiscreteVariableValues(tree.evidence())) {
				///	double currentVal = sufficientStatistics.get(row);
					//calculate the joint probability of the variable and its parents
					//since the table scope is used to find the clique node all the rows discrete values shoudl be there
					//double probability = tree.jointProbabilityOfVariables(this.table.scope()).getRowByValues(false, row.variableValueSet()).getValue();
					//totalContribution += probability;
					//sufficientStatistics.put(row, currentVal + probability);
					
				//}
			//}
			
			if(Math.abs(totalContribution - 1) > 0.001) {
				throw new IllegalStateException("Error total contribution is not 1!");
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
