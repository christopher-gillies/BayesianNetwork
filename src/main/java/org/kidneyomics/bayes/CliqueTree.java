package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kidneyomics.graph.MinNeighborsEvaluationMetric;

class CliqueTree {

	//construct with variable elimination
	
	//use this map to figure out which node are having messages sent to them
	private HashMap<TableFactor,CliqueNode> tauToNodeMap = new HashMap<TableFactor, CliqueTree.CliqueNode>();
	private Set<CliqueNode> nodes = new HashSet<CliqueNode>();
	private final TableBayesianNetwork network;
	
	private CliqueTree(TableBayesianNetwork network) {
		this.network = network;
	}
	
	static CliqueTree create(TableBayesianNetwork network) {
		CliqueTree tree = new CliqueTree(network);
		
		List<DiscreteVariable> variableEliminationOrder = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		//add nodes to tree
		TableBayesianNetworkUtil.sumProductVariableElimination(network.factors(), variableEliminationOrder, tree);
		
		
		return tree;
	}
	
	
	static CliqueTree createFromNetworkAndOrder(TableBayesianNetwork network, List<DiscreteVariable> variableEliminationOrder) {
		CliqueTree tree = new CliqueTree(network);
			
		//add nodes to tree
		TableBayesianNetworkUtil.sumProductVariableElimination(network.factors(), variableEliminationOrder, tree);
			
		return tree;
	}
	
	/**
	 * assign all the factors from the bayesian network to the clique tree
	 */
	void assignFactorsToNodes() {
		Set<TableFactor> factors = network.factors();
		Set<TableFactor> assignedFactors = new HashSet<TableFactor>();
		
		//clear factors from nodes
		for(CliqueNode node : nodes) {
			node.factors.clear();
			node.initial = null;
		}
		
		
		for(TableFactor factor : factors) {
			for(CliqueNode node : nodes) {
				
				//add factor to node if its scope is totally within the node's scope
				if(node.scope.containsAll(factor.scope())) {
					node.factors.add(factor);
					assignedFactors.add(factor);
					break;
				}
			}
		}
		
		if(factors.size() != assignedFactors.size()) {
			throw new IllegalStateException("Not all factors have been assigned to the clique tree");
		}
	}
	
	/**
	 * Apply Theorem 10.6 to the tree
	 * essentially for each node compare it with all other nodes. if a node's scope is a proper subset of another node then prune it
	 */
	void pruneNonmaximalNodes() {
		int size = this.nodes.size();
		CliqueNode nodesArray[] = new CliqueNode[size];
		nodesList().toArray(nodesArray);
		
		for(int i = 0; i < nodesArray.length; i++) {
			CliqueNode current = nodesArray[i];
			for(int j = 0; j < nodesArray.length; j++) {
				CliqueNode cmp = nodesArray[j];
				
				if(cmp.scope.containsAll(current.scope) && cmp.scope.size() > current.scope.size()) {
					pruneNode(current);
					break;
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param node to prune
	 * set a neighbor l for node
	 * connected all the neighbors of the node to l except l and remove node from the node set and all its neighbors
	 */
	void pruneNode(CliqueNode node) {
		
		if(!this.nodes.contains(node)) {
			throw new IllegalArgumentException("node is not found in this tree");
		}
		
		List<CliqueNode> neighbors = node.neighbors();
		if(neighbors.size() == 0) {
			throw new IllegalArgumentException("Cannot prune a node without neighbors");
		}
		
		
		Iterator<CliqueNode> iter = neighbors.iterator();
		CliqueNode target = iter.next();
		node.removeNeighbor(target);
		while(iter.hasNext()) {
			CliqueNode next = iter.next();
			target.addNeighbor(next, node.sepSet(next));
			node.removeNeighbor(next);
		}
		
		nodes.remove(node);
	}
	
	Set<CliqueNode> nodes() {
		return this.nodes;
	}
	
	List<CliqueNode> nodesList() {
		ArrayList<CliqueNode> nodeList = new ArrayList<CliqueTree.CliqueNode>(nodes.size());
		nodeList.addAll(nodes);
		Collections.sort(nodeList);
		return nodeList;
	}
	
	/**
	 * 
	 * @param searchScope -- variables the node must have in its scope
	 * @return CliqueNodes containing that scope
	 */
	List<CliqueNode> getNodesByScope(DiscreteVariable... searchScope) {
		Set<DiscreteVariable> set = new HashSet<DiscreteVariable>();
		for(DiscreteVariable var : searchScope) {
			set.add(var);
		}
		return getNodesByScope(set);
	}
	
	/**
	 * 
	 * @param searchScope -- set of variables the node must have in its scope
	 * @return CliqueNodes containing that scope
	 */
	List<CliqueNode> getNodesByScope(Set<DiscreteVariable> searchScope) {
		List<CliqueNode> result = new LinkedList<CliqueNode>();
		
		for(CliqueNode node : nodes) {
			if(node.scope.containsAll(searchScope)) {
				result.add(node);
			}
		}
		return result;
	}
	
	
	/**
	 * Called from Variable Elimination algorithm
	 * @param tauBeforeMarginalize -- defines to scope of the factors for this node
	 * @param tauAfterMarginalize -- stores a map of which tau's go with which nodes. This is used for making edges in the graph
	 * @param factorsUsedToComputeTau
	 * @return the tree back
	 */
	CliqueTree addNode(TableFactor tauBeforeMarginalize, TableFactor tauAfterMarginalize, Set<TableFactor> factorsUsedToComputeTau) {
		
		CliqueNode node = new CliqueNode(tauBeforeMarginalize.scope(), nodes.size());
		nodes.add(node);
		tauToNodeMap.put(tauAfterMarginalize, node);
		
		
		for(TableFactor message : factorsUsedToComputeTau) {
			if(tauToNodeMap.containsKey(message)) {
				node.addNeighbor(tauToNodeMap.get(message), new SepSet(message.scope()));
			}
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(CliqueNode node : nodesList()) {
			sb.append(node.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	class SepSet {
		
		private final Set<DiscreteVariable> set = new HashSet<DiscreteVariable>();
		
		SepSet(Set<DiscreteVariable> set) {
			this.set.addAll(set);
		}
		
		
		boolean setContains(DiscreteVariable... vars) {			
			for(DiscreteVariable var : vars) {
				if(!set.contains(var)) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			return TableBayesianNetworkUtil.setToString(this.set);
		}
		
	}
	
	class CliqueNode implements Comparable<CliqueNode> {
		
		
		//need concept of forward message and backward message
		CliqueNode(Set<DiscreteVariable> scope, int number) {
			
			this.scope.addAll(scope);
			this.number = number;
		}
		
		final int number;
		
		private final Set<DiscreteVariable> scope = new HashSet<DiscreteVariable>();
		private final Set<TableFactor> factors = new HashSet<TableFactor>();
		private TableFactor initial;
		
		private final HashMap<CliqueNode,SepSet> neighbors = new HashMap<CliqueNode,SepSet>();
		
		private final HashMap<CliqueNode,TableFactor> upwardMessages = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		private final HashMap<CliqueNode,TableFactor> downwardMessages = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		
		/**
		 * 
		 * @param vars -- DiscreteVariables to check and see if they are in the scope
		 * @return true if all the variables are in the scope. empty set will return true
		 */
		boolean scopeContains(DiscreteVariable... vars) {			
			for(DiscreteVariable var : vars) {
				if(!scope.contains(var)) {
					return false;
				}
			}
			return true;
		}
		
		void initialize() {
			//initialize
			Iterator<TableFactor> iter = factors.iterator();
			TableFactor current = iter.next();
			while(iter.hasNext()) {
				current = (TableFactor) current.product(iter.next());
			}
			initial = current;
		}
		
		Set<TableFactor> factors() {
			return factors;
		}
		
		CliqueNode addNeighbor(CliqueNode node, SepSet sep) {
			neighbors.put(node,sep);
			node.neighbors.put(this,sep);
			return this;
		}
		
		CliqueNode removeNeighbor(CliqueNode node) {
			neighbors.remove(node);
			node.neighbors.remove(this);
			return this;
		}
		
		CliqueNode receiveMessage(CliqueNode node, TableFactor message) {
			downwardMessages.put(node, message);
			return this;
		}
		
		SepSet sepSet(CliqueNode neighbor) {
			return this.neighbors.get(neighbor);
		}
		
		boolean hasNeighbor(CliqueNode node) {
			return this.neighbors.keySet().contains(node);
		}
		
		List<CliqueNode> neighbors() {
			List<CliqueNode> list = new ArrayList<CliqueTree.CliqueNode>(neighbors.size());
			for(CliqueNode node : neighbors.keySet()) {
				list.add(node);
			}
			return list;
		}
		
		String scopeString() {
			return TableBayesianNetworkUtil.setToString(this.scope);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.number + ":\n");
			sb.append("Scope: { ");
			
			sb.append(scopeString());
			
			sb.append(" }");
			sb.append("\n");
			
			if(this.factors.size() > 0) {
				sb.append("Factors: { ");
				
				Iterator<TableFactor> iterF = this.factors.iterator();
				while(iterF.hasNext()) {
					sb.append("[ ");
					sb.append(TableBayesianNetworkUtil.setToString(iterF.next().scope()));
					sb.append(" ]");
					if(iterF.hasNext()) {
						sb.append(", ");
					}
				}
				
				sb.append(" }");
				sb.append("\n");
			}
			
			sb.append("Neighbors: { ");
			Iterator<Map.Entry<CliqueNode, SepSet>> iter2 = neighbors.entrySet().iterator();
			while(iter2.hasNext()) {
				Map.Entry<CliqueNode, SepSet> next = iter2.next();
				sb.append("< " );
				sb.append(next.getValue().toString());
				sb.append(" >" );
				sb.append(" -- [ " );
				sb.append(next.getKey().scopeString());
				sb.append(" ]" );
				if(iter2.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append(" }");
			
			sb.append("\n");
			
			return sb.toString();
		}

		public int compareTo(CliqueNode o) {
			return this.number - o.number;
		}
		
	}
}
