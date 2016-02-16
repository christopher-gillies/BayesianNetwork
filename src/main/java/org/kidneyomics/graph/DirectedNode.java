package org.kidneyomics.graph;

import java.util.LinkedList;
import java.util.List;

public class DirectedNode<T> {

	private final T payload;
	private final List<DirectedNode<T>> children;
	private final List<DirectedNode<T>> parents;
	
	private DirectedNode(T payload) {
		this.payload = payload;
		this.parents = new LinkedList<DirectedNode<T>>();
		this.children = new LinkedList<DirectedNode<T>>();
	}
	
	
	public static <T> DirectedNode<T> create(T payload) {
		return new DirectedNode<T>(payload);
	}
	
	public DirectedNode<T> addChild(DirectedNode<T> child) {
		this.children.add(child);
		child.parents.add(this);
		return this;
	}
	
	public DirectedNode<T> removeChild(DirectedNode<T> child) {
		this.children.remove(child);
		child.parents.remove(this);
		return this;
	}
	
	public DirectedNode<T> addParent(DirectedNode<T> parent) {
		//make symmetry
		this.parents.add(parent);
		parent.children.add(this);
		return this;
	}
	
	public DirectedNode<T> removeParent(DirectedNode<T> parent) {
		this.parents.remove(parent);
		parent.children.remove(this);
		return this;
	}
	
	public boolean hasParents() {
		return this.parents.size() != 0;
	}
	
	public boolean hasChildren() {
		return this.parents.size() != 0;
	}
	
	public T payload() {
		return this.payload;
	}
	
	public List<DirectedNode<T>> children() {
		return children;
	}
	
	public List<DirectedNode<T>> parents() {
		return parents;
	}
	
	@Override
	public String toString() {
		return this.payload.toString();
	}
	
}
