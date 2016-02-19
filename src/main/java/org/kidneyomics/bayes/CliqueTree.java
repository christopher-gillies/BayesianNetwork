package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Collection;
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
	private boolean pruned = false;
	private final HashMap<DiscreteVariable,List<TableFactor>> belifsPerVariable;
	private final Set<DiscreteVariableValue> evidence = new HashSet<DiscreteVariableValue>();
	private boolean calibrated = false;
	private CliqueTree(TableBayesianNetwork network) {
		this.network = network;
		this.belifsPerVariable = new HashMap<DiscreteVariable, List<TableFactor>>();
	}
	
	static CliqueTree create(TableBayesianNetwork network, DiscreteVariableValue... evidence) {
		HashSet<DiscreteVariableValue> evi = new HashSet<DiscreteVariableValue>();
		for(DiscreteVariableValue valVar : evidence) {
			evi.add(valVar);
		}
		
		return createFromEvidence(network, evi);
	}
	
	static CliqueTree createFromEvidence(TableBayesianNetwork network, Collection<DiscreteVariableValue> evidence) {

		
		List<DiscreteVariable> variableEliminationOrder = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());

		return createFromEvidenceAndOrder(network, evidence, variableEliminationOrder);
	}
	
	
	static CliqueTree createFromEvidenceAndOrder(TableBayesianNetwork network, Collection<DiscreteVariableValue> evidence, List<DiscreteVariable> variableEliminationOrder) {
		CliqueTree tree = new CliqueTree(network);
		tree.evidence.addAll(evidence);
		
		//add nodes to tree
		TableBayesianNetworkUtil.sumProductVariableElimination(network.factors(), variableEliminationOrder, tree);
		
		
		return tree;
	}
	
	
	static CliqueTree createFromNetworkAndOrder(TableBayesianNetwork network, List<DiscreteVariable> variableEliminationOrder ) {
		CliqueTree tree = new CliqueTree(network);
			
		//add nodes to tree
		TableBayesianNetworkUtil.sumProductVariableElimination(network.factors(), variableEliminationOrder, tree);
			
		return tree;
	}
	
	

	public void printBeliefs() {
		for(CliqueNode node : this.nodes) {
			System.out.println(node.toString());
			System.out.println(node.belief());
		}
	}
	
	
	public boolean validateCalibration() {
		for(CliqueNode node : this.nodes) {
			for(CliqueNode neighbor : node.neighbors()) {
				SepSet forNeighbor = node.sepSet(neighbor);
				TableFactor one = (TableFactor) node.belief().marginalize( forNeighbor.getSetDifferenceOfNodeAndSepSet(node)  );
				TableFactor two = (TableFactor) neighbor.belief().marginalize( neighbor.sepSet(node).getSetDifferenceOfNodeAndSepSet(neighbor)  );
				
				System.err.println("Table one");
				System.err.println(one);
				
				
				System.err.println("Table two");
				System.err.println(two);
				
				if( one.rows().size() != two.rows().size() ) {
					throw new IllegalStateException("Cliques are not calibrated b/c they have a different number of rows");
				}
				
				int count = 0;
				for(Row rowA : one.rows()) {
					for(Row rowB : two.rows()) {
						if(rowA.hasAllDiscreteVariableValues(rowB.variableValueSet())) {
							count++;
							if( Math.abs(rowA.getValue() - rowB.getValue()) > 0.0001 ) {
								throw new IllegalStateException("Clique tree is not calibrated");
							}
						}
					}
				}
				
				if(count != one.rows().size()) {
					throw new IllegalStateException("Not enough matching rows");
				}
			}
		}
		
	
		
		return true;
	}
	
	/**
	 * assign all the factors from the bayesian network to the clique tree
	 */
	void assignFactorsToNodesAndInitialize() {
		Set<TableFactor> factors = network.factors();
		Set<TableFactor> assignedFactors = new HashSet<TableFactor>();
		
		if(this.evidence.size() > 0) {
			//loop through evidence
			
			for(DiscreteVariableValue piece : evidence) {
				TableNode node = network.getNode(piece.variable());
				if(node == null) {
					throw new IllegalArgumentException(piece.variable() + " not found in the network!");
				}
				//get factor
				TableFactor factor = node.factor();
				//remove this factor from set of factors
				factors.remove(factor);
				//remove non-evidence rows from factor
				factor = (TableFactor) factor.reduce(piece);
				//add factor back to the set
				factors.add(factor);
			}
		}
		
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
		
		initializeCliques();
	}
	
	void initializeCliques() {
		//initialize cliques
		for(CliqueNode node : nodes) {
			node.initialize();
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
		
		//set state of this tree as pruned
		pruned = true;
		
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
			SepSet sepForNext = node.sepSet(next);
			node.removeNeighbor(next);
			//reversing these orders will result in a missing pointer in sep hash table for next node
			target.addNeighbor(next, sepForNext);
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
				CliqueNode messagingNode = tauToNodeMap.get(message);
				node.addNeighbor(messagingNode, new SepSet(message.scope()));
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
	
	boolean isCalibrated() {
		return calibrated;
	}
	
	//algorithm 10.2
	void calibrateCliqueTree() {
		//clear old beliefs
		this.belifsPerVariable.clear();
		
		if(!pruned) {
			pruneNonmaximalNodes();
		}
		
		//this will assign factors and initialize cliques
		assignFactorsToNodesAndInitialize();
		
		for(CliqueNode node : this.nodes) {
			
			if(!node.hasReceivedAllMessages() || node.belief() == null) {
				node.requestMessagesAndComputeResult();
			}
			
		}
		
		//Store beliefs per variable
		for(CliqueNode node : this.nodes) {
			for(DiscreteVariable var : node.scope) {
				if(belifsPerVariable.containsKey(var)) {
					belifsPerVariable.get(var).add(node.belief());
				} else {
					List<TableFactor> beliefs = new LinkedList<TableFactor>();
					beliefs.add(node.belief());
					
					belifsPerVariable.put(var, beliefs);
				}
			}
		}
		
		calibrated = true;
		
	}
	
	
	/**
	 * 
	 * @param var (V)
	 * @return P(V | E)
	 */
	TableProbabilityDistribution marginalProbabilityNormalized(DiscreteVariable var) {
		return TableProbabilityDistribution.create(marginalProbabilityUnnormalized(var));
	}
	
	
	TableFactor marginalProbabilityUnnormalized(DiscreteVariable var) {
		if(this.belifsPerVariable == null || this.belifsPerVariable.size() == 0) {
			calibrateCliqueTree();
		}
		
		if(!this.belifsPerVariable.containsKey(var)) {
			throw new IllegalArgumentException("Variable " + var + " not found!");
		}
		
		TableFactor beleif = this.belifsPerVariable.get(var).get(0);
		
		HashSet<DiscreteVariable> variablesToMarginalizeOut = new HashSet<DiscreteVariable>();
		variablesToMarginalizeOut.addAll(beleif.scope());
		variablesToMarginalizeOut.remove(var);
		
		return (TableFactor) beleif.marginalize(variablesToMarginalizeOut);
	}
	
	/**
	 * 
	 * @return probability of the evidence used to construct this tree
	 */
	double probabilityOfEvidenceMarginalizedOverMissingValues(boolean log) {
		
		//choose smallest clique distribution
		TableFactor beliefToUse = null;
		int size = Integer.MAX_VALUE;
		for(CliqueNode node : this.nodes) {
			
			if(node.belief() == null) {
				throw new IllegalStateException("Please calibrate the tree");
			}
			
			if(node.belief().rows().size() < size) {
				size = node.belief().rows().size();
				beliefToUse = node.belief();
			}
		}
		
		if(beliefToUse == null) {
			throw new IllegalStateException("Error cannot be null");
		}
		
		//return the sum of the entries
		double logSum = ProbabilityDistributionUtil.sumLogScale(beliefToUse.rows());
		
		if(log) {
			return logSum;
		} else {
			return Math.exp(logSum);
		}
	}
	
	class SepSet {
		
		private final Set<DiscreteVariable> set = new HashSet<DiscreteVariable>();
		private final HashMap<CliqueNode,Set<DiscreteVariable>> cliqueMinusSepSet = new HashMap<CliqueNode,Set<DiscreteVariable>>();
		
		//set up on creation...
		//but what if we prune....
		SepSet(Set<DiscreteVariable> set) {
			this.set.addAll(set);
		}
		
		Set<DiscreteVariable> getSetDifferenceOfNodeAndSepSet(CliqueNode node) {
			return this.cliqueMinusSepSet.get(node);
		}
		
		void add(CliqueNode node) {
			//create set of variables in node scope but not in the sepset
			HashSet<DiscreteVariable> nodeMinusSepSet = new HashSet<DiscreteVariable>();
			for(DiscreteVariable var : node.scope) {
				if(!set.contains(var)) {
					nodeMinusSepSet.add(var);
				}
			}
			cliqueMinusSepSet.put(node, nodeMinusSepSet);
		}
		
		void remove(CliqueNode node) {
			cliqueMinusSepSet.remove(node);
		}
		
		void clearAll() {
			//this.set.clear();
			this.cliqueMinusSepSet.clear();
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
		private TableFactor initial = null;
		private TableFactor belief = null;
		
		private final HashMap<CliqueNode,SepSet> neighbors = new HashMap<CliqueNode,SepSet>();
		
		private final HashMap<CliqueNode,TableFactor> sending = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		private final HashMap<CliqueNode,TableFactor> received = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		
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
		
		TableFactor belief() {
			return this.belief;
		}
		
		//compute the joint distribution of the initial factors
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
			sep.add(node);
			sep.add(this);
			neighbors.put(node,sep);
			node.neighbors.put(this,sep);
			return this;
		}
		
		CliqueNode removeNeighbor(CliqueNode node) {
			if(this.neighbors.containsKey(node)) {
				//adjust sepset 
				SepSet sep = neighbors.get(node);
				sep.remove(node);
				sep.remove(this);
				neighbors.remove(node);
				node.neighbors.remove(this);
				return this;
			} else {
				throw new IllegalArgumentException("This node does not contain: " + node.toString() );
			}
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
		
		TableFactor sendMessage(CliqueNode receiver) {
			if(readyToSendTo(receiver)) {
				//send message
				receiver.receiveMessage(this, sending.get(receiver));
			} else {
				//check if ready
				for(CliqueNode sender : this.neighbors.keySet()) {
					//we do not request a message from the node we are trying to send to
					if(sender.equals(receiver)) {
						continue;
					}
					
					if(!this.receivedMessageFrom(sender)) {
						//request message from the required sender
						//this recursion will terminate b/c of leaf nodes
						sender.requestMessageToBeSent(this);
					}
				}
				
				//compute
				//SP-message procedure from Algorithm 10.1
				
				//compute product
				TableFactor current = this.initial;
				for(CliqueNode sender : this.neighbors.keySet()) {
					//we do not use the message from the node we are trying to send to
					//this would be circular
					if(sender.equals(receiver)) {
						continue;
					}
					
					current = (TableFactor) current.product(this.received.get(sender));	
				}
				//marginalize variables not in sepset
				
				/*
				 * 
				 * 
				 *    receiver1<--->sepset1<--->node<--->sepset2<--->receiver2
				 * 
				 */
				SepSet sepSetForReceiver = sepSet(receiver);
				Set<DiscreteVariable> setDifference = sepSetForReceiver.getSetDifferenceOfNodeAndSepSet(this);
				if(setDifference == null) {
					throw new IllegalStateException("No set found for: " + this.toString());
				}
				current = (TableFactor) current.marginalize(setDifference);
				this.sending.put(receiver, current);
				
				//send message
				receiver.receiveMessage(this, current);
			}
			
			return this.sending.get(receiver);
		}
		
		void requestMessageToBeSent(CliqueNode requester) {
			//send the message to the requester
			sendMessage(requester);
		}
		
		void receiveMessage(CliqueNode sender, TableFactor message) {
			if(this.neighbors.keySet().contains(sender)) {
				this.received.put(sender, message);
			} else {
				throw new IllegalArgumentException("The following node is not a neighbor of " + this.toString() + "\n" + sender.toString());
			}
		}
		
		/**
		 * 
		 * @param receiver 
		 * @return true if this clique is ready to send a message to the receiver, false otherwise
		 */
		boolean readyToSendTo(CliqueNode receiver) {
			if(this.neighbors.keySet().contains(receiver)) {
				
				if(this.sending.containsKey(receiver)) {
					return true;
				} else {
					return false;
				}
				
			} else {
				throw new IllegalArgumentException("The following node is not a neighbor of " + this.toString() + "\n" + receiver.toString());
			}
		}
		
		/**
		 * 
		 * @param sender
		 * @return true if this clique has received a message from the sender
		 */
		boolean receivedMessageFrom(CliqueNode sender) {
			if(this.neighbors.keySet().contains(sender)) {
				if(this.received.containsKey(sender)) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new IllegalArgumentException("The following node is not a neighbor of " + this.toString() + "\n" + sender.toString());
			}
		}
		
		/**
		 * 
		 * @return true if this clique has received all its messages from its neighbors
		 */
		boolean hasReceivedAllMessages() {
			return this.received.keySet().containsAll(this.neighbors());
		}
		
		/**
		 * Request all messages from neighbors if they have not already been sent
		 */
		TableFactor requestMessagesAndComputeResult() {
			for(CliqueNode sender : neighbors.keySet()) {
				if(!receivedMessageFrom(sender)) {
					sender.requestMessageToBeSent(this);
				}
			}
			belief = initial;
			for(TableFactor message : this.received.values()) {
				belief = (TableFactor) belief.product(message);
			}
			return belief;
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
