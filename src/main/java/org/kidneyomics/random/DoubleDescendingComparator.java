package org.kidneyomics.random;

import java.util.Comparator;

public class DoubleDescendingComparator implements Comparator<Double> {

	public int compare(Double o1, Double o2) {
		return -1 * o1.compareTo(o2);
	}
	
}
