package org.kidneyomics.graph;

import java.util.HashMap;
import java.util.HashSet;
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
	
	/**
	 * 
	 * @param order -- topological ordering
	 * @param nodes -- the graph
	 * @return true if the order is a valid topological sort and false otherwise
	 */
	public static <T> boolean validOrder(List<T> order, List<DirectedNode<T>> nodes) {
		
		if(order.size() != nodes.size()) {
			return false;
		}
		
		//construct map
		//T ---> DirectedNode<T>
		HashMap<T,DirectedNode<T>> map = new HashMap<T, DirectedNode<T>>();
		for(DirectedNode<T> node : nodes) {
			map.put(node.payload(), node);
		}
		
		HashSet<T> marked = new HashSet<T>();
		
		for(T item : order) {
			DirectedNode<T> node = map.get(item);
			for(DirectedNode<T> parent : node.parents()) {
				// if parent has not already been visited then is is not a valid topological sort
				// b/c a topological sort requires all dependencies to be filled before perfoming an action
				if(!marked.contains(parent.payload())) {
					return false;
				}
			}
			marked.add(item);
		}
		
		return true;
	}
}
