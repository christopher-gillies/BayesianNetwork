package org.kidneyomics.bayes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class CliqueTree {

	//construct with variable elimination
	
	private class CliqueNode {
		
		
		//need concept of forward message and backward message
		CliqueNode(Set<TableFactor> factors) {
			
			
			this.factors.addAll(factors);
			
			//initialize
			Iterator<TableFactor> iter = factors.iterator();
			TableFactor current = iter.next();
			while(iter.hasNext()) {
				current = (TableFactor) current.product(iter.next());
			}
			initial = current;
		}
		
		final Set<TableFactor> factors = new HashSet<TableFactor>();
		final TableFactor initial;
		
		final Set<CliqueNode> neighbors = new HashSet<CliqueNode>();
		
		final HashMap<CliqueNode,TableFactor> upwardMessages = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		final HashMap<CliqueNode,TableFactor> downwardMessages = new HashMap<CliqueTree.CliqueNode, TableFactor>();
		
		CliqueNode addNeighbor(CliqueNode node) {
			neighbors.add(node);
			return this;
		}
		
		CliqueNode receiveMessage(CliqueNode node, TableFactor message) {
			downwardMessages.put(node, message);
			return this;
		}
		
		//boolean isReady() {
		//	return false
		//}
		
	}
}
