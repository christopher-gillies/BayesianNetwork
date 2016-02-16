package org.kidneyomics.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UndirectedNode<T> {

	private final T payload;
	
	//no double edges
	private final Set<UndirectedNode<T>> neighbors;
	
	
	private UndirectedNode(T payload) {
		this.neighbors = new HashSet<UndirectedNode<T>>();
		this.payload = payload;
	}
	
	public static <T> UndirectedNode<T> create(T payload) {
		return new UndirectedNode<T>(payload);
	}
	
	
	public UndirectedNode<T> addNeighbor(UndirectedNode<T> neighbor) {
		//symmetry
		neighbors.add(neighbor);
		neighbor.neighbors.add(this);
		return this;
	}
	
	public UndirectedNode<T> removeNeighbor(UndirectedNode<T> neighbor) {
		neighbors.remove(neighbor);
		return this;
	}
	
	
	public Set<UndirectedNode<T>> neighbors() {
		return this.neighbors;
	}
	
	public T payload() {
		return payload;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(payload.toString());
		sb.append("\n");
		if(neighbors.size() > 0) {
			sb.append("Neighbors: ");
			Iterator<UndirectedNode<T>> iter = neighbors.iterator();
			while(iter.hasNext()) {
				
				sb.append(iter.next().payload.toString());
				if(iter.hasNext()) {
					sb.append(", ");
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Add edges between all neighbors for this node
	 * @return this
	 */
	public UndirectedNode<T> addEdgesBetweenNeighbors() {
		ArrayList<UndirectedNode<T>> neighborsList = new ArrayList<UndirectedNode<T>>(neighbors.size());
		neighborsList.addAll(neighbors);
		for(int i = 0; i < neighborsList.size() - 1; i++) {
			UndirectedNode<T> nodeA = neighborsList.get(i);
			for(int j = i + 1; j < neighborsList.size(); j++) {
				UndirectedNode<T> nodeB = neighborsList.get(j);
				nodeA.addNeighbor(nodeB);
			}
		}
		return this;
	}
}
