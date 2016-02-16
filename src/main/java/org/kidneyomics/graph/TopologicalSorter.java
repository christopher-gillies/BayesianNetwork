package org.kidneyomics.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TopologicalSorter {

	/**
	 * Implementation of Kahn's algorithm
	 * https://en.wikipedia.org/wiki/Topological_sorting
	 * @param nodes
	 * @return
	 */
	public static <T> List<T> sort(List<DirectedNode<T>> nodes) {
		LinkedList<T> topologicallySortedElements = new LinkedList<T>();
		
		LinkedList<DirectedNode<T>> nodesWithNoIncomingEdges = new LinkedList<DirectedNode<T>>();
		
		for(DirectedNode<T> node : nodes) {
			if(!node.hasParents()) {
				nodesWithNoIncomingEdges.add(node);
			}
		}
		
		while(nodesWithNoIncomingEdges.peek() != null) {
			DirectedNode<T> node = nodesWithNoIncomingEdges.poll();
			topologicallySortedElements.addLast(node.payload());
			
			List<DirectedNode<T>> children = new LinkedList<DirectedNode<T>>();
			children.addAll(node.children());
			Iterator<DirectedNode<T>> iter = children.iterator();
			while(iter.hasNext()) {
				DirectedNode<T> child = iter.next();
				
				//removes edges
				child.removeParent(node);
				
				//if child does not have any remaining parents then add it to the nodesWithNoIncomingEdges
				if(!child.hasParents()) {
					nodesWithNoIncomingEdges.add(child);
				}
				
			}
		}
		
		//check if any remaining edges
		for(DirectedNode<T> node : nodes) {
			if(node.hasParents() || node.hasChildren()) {
				throw new IllegalStateException("This graph has a cycle!");
			}
		}
		
		//return topologgically sorted elements
		return topologicallySortedElements;
	}
}
