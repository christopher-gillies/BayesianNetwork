package org.kidneyomics.bayes;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import org.kidneyomics.graph.DirectedNode;
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
	
	
}
