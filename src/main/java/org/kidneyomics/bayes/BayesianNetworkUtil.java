package org.kidneyomics.bayes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.kidneyomics.graph.DirectedNode;
import org.kidneyomics.graph.EvaluationMetric;
import org.kidneyomics.graph.UndirectedNode;
import org.kidneyomics.graph.TopologicalSorter;

public class BayesianNetworkUtil {
	
	/**
	 * 
	 * @param nodes from a table network
	 * @return a graph representation of the network
	 */
	public static List<DirectedNode<TableNode>> createGraphFromTableNode(List<TableNode> nodes) {
		List<DirectedNode<TableNode>> graphNodes = new LinkedList<DirectedNode<TableNode>>();
		
		
		//Create look up for nodes
		HashMap<TableNode,DirectedNode<TableNode>> nodeMap = new HashMap<TableNode, DirectedNode<TableNode>>();
		
		for(TableNode node : nodes) {
			nodeMap.put(node, DirectedNode.create(node));
		}
		
		
		//add children to each node
		for(TableNode node : nodes) {
			DirectedNode<TableNode> graphNode = nodeMap.get(node);
			for(TableNode child : node.children()) {
				DirectedNode<TableNode> graphChildNode = nodeMap.get(child);
				graphNode.addChild(graphChildNode);
			}
			
			graphNodes.add(graphNode);
		}
		
		
		return graphNodes;
	}
	
	
	public static List<TableNode> topologicalSort(List<TableNode> nodes) {
		return TopologicalSorter.sort(createGraphFromTableNode(nodes));
	}
	
	
	/**
	 * 
	 * @param nodes from Bayesian network
	 * @return a list of nodes describing an undirected graph for the Bayesian network
	 */
	public static List<UndirectedNode<DiscreteVariable>> createUndirectedGraphFromTableNodes(List<TableNode> nodes) {
		List<UndirectedNode<DiscreteVariable>> graphNodes = new LinkedList<UndirectedNode<DiscreteVariable>>();
		
		//create variable --> graphNode
		HashMap<DiscreteVariable,UndirectedNode<DiscreteVariable>> variable2GraphNode = new HashMap<DiscreteVariable, UndirectedNode<DiscreteVariable>>();
		
		for(TableNode tableNode : nodes) {
			UndirectedNode<DiscreteVariable> graphNode = UndirectedNode.create(tableNode.variable());
			variable2GraphNode.put(tableNode.variable(), graphNode);
			
			//store graph node
			graphNodes.add(graphNode);
		}
		
		//add edges
		//two variables are involved in the same factor then add an edge between them
		for(TableNode tableNode : nodes) {
			ArrayList<DiscreteVariable> list = new ArrayList<DiscreteVariable>(tableNode.factor().scope().size());
			list.addAll(tableNode.factor().scope());
			//loop through all pairs of variables in the factor and add edges between them
			for(int i = 0; i < list.size() - 1; i++) {
				UndirectedNode<DiscreteVariable> graphNodeA = variable2GraphNode.get(list.get(i));
				for(int j = i + 1; j < list.size(); j++) {
					UndirectedNode<DiscreteVariable> graphNodeB = variable2GraphNode.get(list.get(j));
					graphNodeA.addNeighbor(graphNodeB);
				}
			}
		}
		
		return graphNodes;
	}
	
	
	/**
	 * 
	 * @param nodes -- undirected graph nodes list
	 * @return -- return a map where the payload points to each node in the graph
	 */
	public static Map<DiscreteVariable,UndirectedNode<DiscreteVariable>> createGraphNodeMapFromList(List<UndirectedNode<DiscreteVariable>> nodes) {
		HashMap<DiscreteVariable,UndirectedNode<DiscreteVariable>> map = new HashMap<DiscreteVariable, UndirectedNode<DiscreteVariable>>();
		
		for(UndirectedNode<DiscreteVariable> node : nodes) {
			map.put(node.payload(), node);
		}
		
		return map;
	}
	
	/**
	 * Algorithm 9.4 from Probabilistic Graphical Models
	 * An order for performing variable elimination using the supplied metric
	 * @param nodes from undirected graph
	 * @param metric to minimize
	 * @return
	 */
	public static List<DiscreteVariable> greedyVariableEliminationOrder(List<UndirectedNode<DiscreteVariable>> nodes, EvaluationMetric<DiscreteVariable> metric) {
		LinkedList<DiscreteVariable> order = new LinkedList<DiscreteVariable>();
		HashSet<DiscreteVariable> marked = new HashSet<DiscreteVariable>();
		HashSet<UndirectedNode<DiscreteVariable>> unmarked = new HashSet<UndirectedNode<DiscreteVariable>>();
		unmarked.addAll(nodes);
		HashMap<DiscreteVariable,Double> scores = new HashMap<DiscreteVariable,Double>();
		
		//we need to perform this for each variable
		for(int i = 0; i < nodes.size(); i++) {
			
			//compute score per variable
			//only consider unmarked nodes
			for(UndirectedNode<DiscreteVariable> node : unmarked) {
				scores.put(node.payload(), metric.calculate(node));
			}
			
			//find minimum not marked
			double min = Double.MAX_VALUE;
			UndirectedNode<DiscreteVariable> minNode = null;
			for(UndirectedNode<DiscreteVariable> node : unmarked) {
				double score = scores.get(node.payload());
				if(score < min) {
					min = score;
					minNode = node;
				}
			}
			
			if(minNode == null) {
				throw new IllegalStateException("Algorithm in an illegal state");
			}
			
			//mark node
			marked.add(minNode.payload());
			//remove from unmarked
			unmarked.remove(minNode);
			
			//store variable
			order.add(minNode.payload());
			
			//add edges
			minNode.addEdgesBetweenNeighbors();
			
		}
		
		if(marked.size() != nodes.size()) {
			throw new IllegalStateException("Algorithm in an illegal state");
		}
		
		
		return order;
	}
}
