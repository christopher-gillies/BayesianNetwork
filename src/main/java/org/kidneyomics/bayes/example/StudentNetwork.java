package org.kidneyomics.bayes.example;

import java.util.LinkedList;
import java.util.List;

import org.kidneyomics.bayes.BayesianNetwork;
import org.kidneyomics.bayes.BayesianNetworkUtil;
import org.kidneyomics.bayes.DiscreteValue;
import org.kidneyomics.bayes.DiscreteVariable;
import org.kidneyomics.bayes.DiscreteVariableValue;
import org.kidneyomics.bayes.Row;
import org.kidneyomics.bayes.TableFactor;
import org.kidneyomics.bayes.TableNode;
import org.kidneyomics.graph.TopologicalSorter;

public class StudentNetwork implements BayesianNetwork {
	
	private List<TableNode> nodes;
	
	private StudentNetwork() {
		this.nodes = new LinkedList<TableNode>();
		
		
		
		//create nodes
		
		//create difficulty node
		TableNode diffNode = null;
		{
			DiscreteVariable diffVar = DiscreteVariable.create("Difficulty", 
					DiscreteValue.create("d0"), DiscreteValue.create("d1"));
			
			TableFactor diffFactor = TableFactor.create(diffVar);
			
			diffFactor.addRows( Row.create(0.6, DiscreteVariableValue.create(diffVar, diffVar.getValueByName("d0"))),
					Row.create(0.4, DiscreteVariableValue.create(diffVar, diffVar.getValueByName("d1")))
					);
			
			diffNode = TableNode.create(diffVar, diffFactor);
			nodes.add(diffNode);
		}
		
		
		//create intelligence node
		TableNode intelNode = null;
		{
			DiscreteVariable intelVar = DiscreteVariable.create("Intelligence", 
					DiscreteValue.create("i0"), DiscreteValue.create("i1"));
			
			TableFactor intelFactor = TableFactor.create(intelVar);
			
			intelFactor.addRows( Row.create(0.7, DiscreteVariableValue.create(intelVar, intelVar.getValueByName("i0"))),
					Row.create(0.3, DiscreteVariableValue.create(intelVar, intelVar.getValueByName("i1")))
					);
			
			intelNode = TableNode.create(intelVar, intelFactor);
			nodes.add(intelNode);
		}
		
		
		//create SAT node
		TableNode satNode = null;
		{
			DiscreteVariable satVar = DiscreteVariable.create("SAT", 
					DiscreteValue.create("s0"), DiscreteValue.create("s1"));
			
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
		}
		
		
		//create Grade node
		TableNode gradeNode = null;
		{
			DiscreteVariable gradeVar = DiscreteVariable.create("Grade", 
					DiscreteValue.create("g1"), DiscreteValue.create("g2"), DiscreteValue.create("g3"));
			
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
		}
		
		//create letter node
		TableNode letterNode = null;
		{
			DiscreteVariable letterVar = DiscreteVariable.create("Letter", 
					DiscreteValue.create("l0"), DiscreteValue.create("l1"));
			
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
					Row.create(0.1, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g2")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l0"))
					),
					Row.create(0.9, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g2")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l1"))
					),
					//g3
					Row.create(0.1, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g3")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l0"))
					),
					Row.create(0.9, DiscreteVariableValue.create(gradeNode.variable(), gradeNode.variable().getValueByName("g3")),
					DiscreteVariableValue.create(letterVar, letterVar.getValueByName("l1"))
					)
					);
			
			letterNode = TableNode.create(letterVar, letterFactor);
			
			//add parent node
			letterNode.addParent(gradeNode);
			
			
			nodes.add(letterNode);
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
	
	public static StudentNetwork create() {
		return new StudentNetwork();
	}
	
	public List<TableNode> topologicalSort() {
		return TopologicalSorter.sort(BayesianNetworkUtil.createGraphFromTableNode(nodes));
	}
	
}
