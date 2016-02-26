package org.kidneyomics.bayes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kidneyomics.bayes.json.JSON_CPD;
import org.kidneyomics.bayes.json.JSON_CPD_Row;
import org.kidneyomics.bayes.json.JSON_Node;
import org.kidneyomics.bayes.json.JSON_TableBayesianNetwork;

public class TableBayesianNetworkImpl implements TableBayesianNetwork {

	private final List<TableNode> nodes;
	
	private final String name;
	
	//variable reference ---> table node
	private final Map<DiscreteVariable,TableNode> nodesMap = new HashMap<DiscreteVariable,TableNode>();
	
	// variable string name ---> variable reference
	private final Map<String,DiscreteVariable> stringToVariable = new HashMap<String, DiscreteVariable>();
	
	private TableBayesianNetworkImpl(String name) {
		this.name = name;
		this.nodes = new ArrayList<TableNode>();
	}
	
	
	public static TableBayesianNetworkImpl createFromJSON(JSON_TableBayesianNetwork jsonNetwork) {
		TableBayesianNetworkImpl network = new TableBayesianNetworkImpl(jsonNetwork.name);

		
		//Create variables
		for(JSON_Node node : jsonNetwork.nodes) {
			
			
			//Create variable values
			HashSet<DiscreteValue> values = new HashSet<DiscreteValue>();
			for(String value : node.cpd.columns) {
				values.add(DiscreteValue.create(value));
			}
			
			//Create variable
			DiscreteVariable variable = DiscreteVariable.create(node.name, values);
			
			//store in hash table
			network.stringToVariable.put(variable.getName(), variable);
			
			
		}
		
		
		//Create nodes
		for(JSON_Node node : jsonNetwork.nodes) {
			JSON_CPD cpd = node.cpd;
			
			DiscreteVariable variable = network.stringToVariable.get(node.name);
			HashSet<DiscreteVariable> scope = new HashSet<DiscreteVariable>();
			
			//create the scope of the variable
			scope.add(variable);
		
			DiscreteVariable parents[] = new DiscreteVariable[node.parents.size()];
			int parentIndex = 0;
			for(String parent : node.parents) {
				DiscreteVariable parVar = network.stringToVariable.get(parent);
				if(parVar == null) {
					throw new IllegalStateException(parent + " variable not found");
				}
				scope.add(parVar);
				parents[parentIndex++] = parVar;
			}
			
			//create table factor
			TableFactor factor = TableFactor.create(scope);
			
			int index = 0;
			DiscreteVariableValue[] columnToVarVal = new DiscreteVariableValue[node.cpd.columns.size()];
			
			for(String column : node.cpd.columns) {
				DiscreteValue value = variable.getValueByName(column);
				if(value == null) {
					throw new IllegalStateException(value + " variable not found");
				}
				columnToVarVal[index++] = variable.getVariableValueByName(column);
			}
			
			//add all the rows to the factor
			for(JSON_CPD_Row cpdRow : node.cpd.rows) {
				
				//add parent row labels
				HashSet<DiscreteVariableValue> parentRowLabels = new HashSet<DiscreteVariableValue>();
				if(cpdRow.labels.size() > parents.length) {
					throw new IllegalStateException("More labels than parents for " + node.name);
				}
				
				for(int i = 0; i < cpdRow.labels.size(); i++) {
					String label = cpdRow.labels.get(i);
					DiscreteVariable parent = parents[i];
					DiscreteVariableValue varVal = parent.getVariableValueByName(label);
					
					if(varVal == null) {
						throw new IllegalStateException(label + " not found for " + parent.getName() + " in " + node.name);
					}
					
					parentRowLabels.add(varVal);	
				}
				
				if(cpdRow.values.size() > columnToVarVal.length) {
					throw new IllegalStateException("More values than columns for " + node.name);
				}
				
				for(int i = 0; i < cpdRow.values.size(); i++) {
					//add all row labels
					DiscreteVariableValue varVal = columnToVarVal[i];
					HashSet<DiscreteVariableValue> rowLabels = new HashSet<DiscreteVariableValue>();
					rowLabels.addAll(parentRowLabels);
					rowLabels.add(varVal);
					
					
					double prob = cpdRow.values.get(i);
					
					//Add row
					Row row = Row.create(prob, rowLabels);
					factor.addRow(row);
				}
				
			}
			
			//create table node
			TableNode tableNode = TableNode.create(variable, factor);
			
			//add table node to node map
			network.nodes.add(tableNode);
			network.nodesMap.put(variable, tableNode);
		}
		
		
		//Create edges
		for(JSON_Node node : jsonNetwork.nodes) {
			DiscreteVariable var = network.stringToVariable.get(node.name);
			TableNode tableNode = network.getNode(var);
			
			for(String parent : node.parents) {
				DiscreteVariable parVar = network.stringToVariable.get(parent);
				TableNode parNode = network.getNode(parVar);
				
				if(parVar == null || parNode == null) {
					throw new IllegalStateException(parent + " variable not found");
				}

				tableNode.addParent(parNode);
			}
		}
		
		
		return network;
	}
	
	
	@Override
	public DiscreteVariable getVariableByName(String name) {
		return stringToVariable.get(name);
	}
	
	
	public List<TableNode> topologicalSort() {
		return TableBayesianNetworkUtil.topologicalSort(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(TableNode node : topologicalSort()) {
			sb.append(node.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public List<TableNode> nodes() {
		return this.nodes;
	}

	@Override
	public Set<TableFactor> factors() {
		HashSet<TableFactor> factors = new HashSet<TableFactor>();
		for(TableNode node : nodes) {
			factors.add(node.factor());
		}
		return factors;
	}

	@Override
	public Set<DiscreteVariable> variables() {
		return this.nodesMap.keySet();
	}

	@Override
	public TableNode getNode(DiscreteVariable variable) {
		return this.nodesMap.get(variable);
	}

	@Override
	public String name() {
		return name;
	}


	@Override
	public JSON_TableBayesianNetwork toJSON() {
		return TableBayesianNetworkUtil.toJSON(this);
	}


	@Override
	public List<DiscreteInstance> forwardSample(int n) {
		return TableBayesianNetworkUtil.forwardSampling(this, n);
	}

}
