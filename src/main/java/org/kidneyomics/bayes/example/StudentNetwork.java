package org.kidneyomics.bayes.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kidneyomics.bayes.BayesianNetwork;
import org.kidneyomics.bayes.TableBayesianNetworkUtil;
import org.kidneyomics.bayes.DiscreteValue;
import org.kidneyomics.bayes.DiscreteVariable;
import org.kidneyomics.bayes.DiscreteVariableValue;
import org.kidneyomics.bayes.Row;
import org.kidneyomics.bayes.TableBayesianNetwork;
import org.kidneyomics.bayes.TableFactor;
import org.kidneyomics.bayes.TableNode;
import org.kidneyomics.bayes.TableProbabilityDistribution;
import org.kidneyomics.graph.TopologicalSorter;

public class StudentNetwork implements TableBayesianNetwork {
	
	private List<TableNode> nodes;
	private TableFactor joint = null;
	private HashMap<String,DiscreteVariable> variableMap;
	private HashMap<DiscreteVariable,TableNode> nodeMap;
	
	private StudentNetwork() {
		this.variableMap = new HashMap<String, DiscreteVariable>();
		this.nodeMap = new HashMap<DiscreteVariable,TableNode>();
		this.nodes = new LinkedList<TableNode>();
		
		
		
		//create nodes
		
		//create difficulty node
		TableNode diffNode = null;
		{
			DiscreteVariable diffVar = DiscreteVariable.create("Difficulty", 
					DiscreteValue.create("d0"), DiscreteValue.create("d1"));
			
			variableMap.put(diffVar.getName(), diffVar);
			
			TableFactor diffFactor = TableFactor.create(diffVar);
			
			diffFactor.addRows( Row.create(0.6, DiscreteVariableValue.create(diffVar, diffVar.getValueByName("d0"))),
					Row.create(0.4, DiscreteVariableValue.create(diffVar, diffVar.getValueByName("d1")))
					);
			
			diffNode = TableNode.create(diffVar, diffFactor);
			nodes.add(diffNode);
			nodeMap.put(diffVar, diffNode);
		}
		
		
		//create intelligence node
		TableNode intelNode = null;
		{
			DiscreteVariable intelVar = DiscreteVariable.create("Intelligence", 
					DiscreteValue.create("i0"), DiscreteValue.create("i1"));
			
			variableMap.put(intelVar.getName(), intelVar);
			
			TableFactor intelFactor = TableFactor.create(intelVar);
			
			intelFactor.addRows( Row.create(0.7, DiscreteVariableValue.create(intelVar, intelVar.getValueByName("i0"))),
					Row.create(0.3, DiscreteVariableValue.create(intelVar, intelVar.getValueByName("i1")))
					);
			
			intelNode = TableNode.create(intelVar, intelFactor);
			nodes.add(intelNode);
			nodeMap.put(intelVar, intelNode);
		}
		
		
		//create SAT node
		TableNode satNode = null;
		{
			DiscreteVariable satVar = DiscreteVariable.create("SAT", 
					DiscreteValue.create("s0"), DiscreteValue.create("s1"));
			
			variableMap.put(satVar.getName(), satVar);
			
			TableFactor satFactor = TableFactor.create(intelNode.variable(),satVar);
			
			satFactor.addRows( 
					Row.create(0.95, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(satVar, satVar.getValueByName("s0"))
					),
					Row.create(0.05, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(satVar, satVar.getValueByName("s1"))
					),
					Row.create(0.2, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(satVar, satVar.getValueByName("s0"))
					),
					Row.create(0.8, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(satVar, satVar.getValueByName("s1"))
					)
					);
			
			satNode = TableNode.create(satVar, satFactor);
			
			//add parent node
			satNode.addParent(intelNode);
			
			
			nodes.add(satNode);
			nodeMap.put(satVar, satNode);
		}
		
		
		//create Grade node
		TableNode gradeNode = null;
		{
			DiscreteVariable gradeVar = DiscreteVariable.create("Grade", 
					DiscreteValue.create("g1"), DiscreteValue.create("g2"), DiscreteValue.create("g3"));
			
			variableMap.put(gradeVar.getName(), gradeVar);
			
			TableFactor gradeFactor = TableFactor.create(intelNode.variable(), diffNode.variable(), gradeVar);
			
			gradeFactor.addRows( 
					
					//i0,d0
					Row.create(0.3, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g1"))
					),
					Row.create(0.4, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g2"))
					),
					Row.create(0.3, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g3"))
					),
					
					//i0,d1
					Row.create(0.05, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g1"))
					),
					Row.create(0.25, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g2"))
					),
					Row.create(0.7, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i0")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g3"))
					),
					
					//i1,d0
					Row.create(0.9, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g1"))
					),
					Row.create(0.08, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g2"))
					),
					Row.create(0.02, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d0")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g3"))
					),
					
					//i1,d1
					Row.create(0.5, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g1"))
					),
					Row.create(0.3, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g2"))
					),
					Row.create(0.2, DiscreteVariableValue.create(intelNode.variable(), intelNode.variable().getValueByName("i1")),
					DiscreteVariableValue.create(diffNode.variable(), diffNode.variable().getValueByName("d1")),
					DiscreteVariableValue.create(gradeVar, gradeVar.getValueByName("g3"))
					)

					);
			
			gradeNode = TableNode.create(gradeVar, gradeFactor);
			
			//add parent node
			gradeNode.addParent(diffNode);
			gradeNode.addParent(intelNode);
			
			
			nodes.add(gradeNode);
			nodeMap.put(gradeVar, gradeNode);
		}
		
		//create letter node
		TableNode letterNode = null;
		{
			DiscreteVariable letterVar = DiscreteVariable.create("Letter", 
					DiscreteValue.create("l0"), DiscreteValue.create("l1"));
			
			variableMap.put(letterVar.getName(), letterVar);
			
			TableFactor letterFactor = TableFactor.create(gradeNode.variable(),letterVar);
			
			letterFactor.addRows( 
					//g1
					Row.create(0.1, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g1")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l0"))
					),
					Row.create(0.9, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g1")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l1"))
					),
					//g2
					Row.create(0.4, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g2")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l0"))
					),
					Row.create(0.6, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g2")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l1"))
					),
					//g3
					Row.create(0.99, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g3")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l0"))
					),
					Row.create(0.01, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g3")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l1"))
					)
					);
			
			letterNode = TableNode.create(letterVar, letterFactor);
			
			//add parent node
			letterNode.addParent(gradeNode);
			
			
			nodes.add(letterNode);
			nodeMap.put(letterVar, letterNode);
		}
		
		
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
	
	public List<TableNode> nodes() {
		return nodes;
	}
	
	public static StudentNetwork create() {
		return new StudentNetwork();
	}
	
	public List<TableNode> topologicalSort() {
		return TableBayesianNetworkUtil.topologicalSort(this);
	}
	
	public TableProbabilityDistribution computeProbability(DiscreteVariable target, DiscreteVariableValue... evidences) {
		
		if(evidences == null) {
			evidences = new DiscreteVariableValue[0];
		}
		
		TableFactor joint = computeJoint();
		
		HashSet<DiscreteVariable> marginalSet = new HashSet<DiscreteVariable>();
		
		marginalSet.addAll(joint.scope());
		
		// remove target
		marginalSet.remove(target);
		
		//remove marginal variables
		for(DiscreteVariableValue varVal : evidences) {
			marginalSet.remove(varVal.variable());
		}
		
		if(evidences.length == 0) {
			return TableProbabilityDistribution.create( (TableFactor)  joint.marginalize(marginalSet));
		} else {
			return TableProbabilityDistribution.create( (TableFactor)  joint.reduce(evidences).marginalize(marginalSet));
		}
			
	}
	
	public DiscreteVariable getVariableByName(String name) {
		return variableMap.get(name);
	}
	
	public TableFactor computeJoint() {
		
		if(joint == null) {
			List<TableNode> sorted = topologicalSort();
			Iterator<TableNode> iter = sorted.iterator();
			TableFactor current = iter.next().factor();
			while(iter.hasNext()) {
				current = (TableFactor) current.product(iter.next().factor());
			}
			joint = current;
			return current;
		} else {
			return joint;
		}
	}
	
	public Set<TableFactor> factors() {
		HashSet<TableFactor> factors = new HashSet<TableFactor>();
		for(TableNode node : nodes) {
			factors.add(node.factor());
		}
		return factors;
	}

	public TableNode getNode(DiscreteVariable variable) {
		return nodeMap.get(variable);
	}
}
