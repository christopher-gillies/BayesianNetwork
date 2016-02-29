package org.kidneyomics.bayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscreteInstance implements Cloneable {
	private final Map<DiscreteVariable,DiscreteVariableValue> values;
	//private final Set<DiscreteVariableValue> valuesSet;
	
	private DiscreteInstance() {
		values = new HashMap<DiscreteVariable, DiscreteVariableValue>();
		//this.valuesSet = new HashSet<DiscreteVariableValue>();
		//this.valuesSet.addAll(values.values());
	}
	
	
	public static DiscreteInstance create() {
		DiscreteInstance instance = new DiscreteInstance();		
		return instance;
	}
	
	public boolean containsMissing() {
		for(Map.Entry<DiscreteVariable, DiscreteVariableValue> entry : values.entrySet()) {
			if(entry.getValue().isMissing()) {
				return true;
			}
		}
		return false;
	}
	
	public void put(DiscreteVariable key, DiscreteVariableValue value) {
		this.values.put(key, value);
	}
	
	public boolean containsKey(DiscreteVariable key) {
		return this.values.containsKey(key);
	}
	
	public boolean containsAllKeys(Collection<DiscreteVariable> keys) {
		return this.values.keySet().containsAll(keys);
	}
	
	public DiscreteVariableValue get(DiscreteVariable key) {
		return this.values.get(key);
	}
	
	public Collection<DiscreteVariableValue> values() {
		return this.values.values();
	}
	
	/**
	 * 
	 * @return all non missing discrete variable entries
	 */
	public HashSet<DiscreteVariableValue> evidence() {
		HashSet<DiscreteVariableValue> res = new HashSet<DiscreteVariableValue>();
		
		for(Map.Entry<DiscreteVariable,DiscreteVariableValue> entry : this.values.entrySet()) {
			DiscreteVariableValue varVal = entry.getValue();
			if(!varVal.isMissing()) {
				res.add(varVal);
			}
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param variables -- variables to subset out
	 * @return subset of variable values for input variables
	 */
	public List<DiscreteVariableValue> subset(Collection<DiscreteVariable> variables) {
		List<DiscreteVariableValue> subset = new ArrayList<DiscreteVariableValue>(variables.size());
		for(DiscreteVariable var : variables) {
			subset.add(get(var));
		}
		return subset;
	}
	
	public String titleRow(Collection<DiscreteVariable> order) {
		StringBuilder sb = new StringBuilder();
		Iterator<DiscreteVariable> iter = order.iterator();
		
		while(iter.hasNext()) {
			DiscreteVariable next = iter.next();
			
			String value = next.getName();
			sb.append(value);
			
			if(iter.hasNext()) {
				sb.append("\t");
			}
		}
		
		return sb.toString();
	}
	
	
	public String toString(Collection<DiscreteVariable> order) {
		StringBuilder sb = new StringBuilder();
		Iterator<DiscreteVariable> iter = order.iterator();
		
		while(iter.hasNext()) {
			DiscreteVariable next = iter.next();
			
			String value = values.get(next).value().toString();
			sb.append(value);
			
			if(iter.hasNext()) {
				sb.append("\t");
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public Object clone() {
		DiscreteInstance instance = create();
		for(DiscreteVariable key : this.values.keySet()) {
			instance.put(key, (DiscreteVariableValue) this.get(key).clone());
		}
		return instance;
	}
	
}
