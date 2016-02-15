package org.kidneyomics.bayes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TableConditionalProbabilityDistribution implements ProbabilityDistribution {

	private final TableFactor table;
	private final DiscreteVariable unconditionedVariable;
	
	private TableConditionalProbabilityDistribution(TableFactor table, DiscreteVariable unconditionedVariable) {
		this.table = table;
		this.unconditionedVariable = unconditionedVariable;
		if(!isNormalized()) {
			normalize();
		}
	}
	
	public TableFactor getFactor() {
		return this.table;
	}
	
	public DiscreteVariable getUnconditionedVaraible() {
		return unconditionedVariable;
	}
	
	public static TableConditionalProbabilityDistribution create(TableFactor table, DiscreteVariable unconditionedVariable) {
		return new TableConditionalProbabilityDistribution(table,unconditionedVariable);
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
					sb.append("\t");
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
		for(Map.Entry<String,HashMap<String,Double>> entry : printMap.entrySet()) {
			
		}
		
		return sb.toString();
	}
	
}
