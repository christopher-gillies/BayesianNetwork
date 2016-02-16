package org.kidneyomics.bayes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TableNode {
	
	private final DiscreteVariable variable;
	private final TableConditionalProbabilityDistribution cpd;
	private final List<TableNode> parents;
	private final List<TableNode> children;
	
	private TableNode(DiscreteVariable variable, TableFactor factor) {
		this.variable = variable;
		this.cpd = TableConditionalProbabilityDistribution.create(factor, variable);
		this.parents = new LinkedList<TableNode>();
		this.children = new LinkedList<TableNode>();
	}
	
	public static TableNode create(DiscreteVariable variable, TableFactor factor) {
		TableNode node = new TableNode(variable, factor);
		
		return node;
	}
	
	
	public List<TableNode> children() {
		return children;
	}
	
	public List<TableNode> parents() {
		return parents;
	}
	
	public TableNode addChild(TableNode child) {
		this.children.add(child);
		child.parents.add(this);
		return this;
	}
	
	
	public TableNode addParent(TableNode parent) {
		this.parents.add(parent);
		parent.children.add(this);
		return this;
	}
	
	public DiscreteVariable variable() {
		return variable;
	}
	
	public String name() {
		return variable.getName();
	}
	
	public TableFactor factor() {
		return this.cpd.getFactor();
	}
	
	public TableConditionalProbabilityDistribution cpd() {
		return this.cpd;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------------------------------\n");
		sb.append(name());
		sb.append("\n---------------------------------------\n");
		sb.append(cpd.toString());
		sb.append("---------------------------------------\n");
		if(children.size() > 0) {
			sb.append("Children: ");
			Iterator<TableNode> iter = children.iterator();
			while(iter.hasNext()) {
				sb.append(iter.next().name());
				if(iter.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append("\n---------------------------------------\n");
		}
		return sb.toString();
	}
	
}
