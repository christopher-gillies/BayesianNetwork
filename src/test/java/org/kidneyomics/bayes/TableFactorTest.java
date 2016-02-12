package org.kidneyomics.bayes;

import java.util.HashSet;

import org.junit.Test;

public class TableFactorTest {
	
	@Test
	public void testCreate() {
		
		HashSet<DiscreteValue> letterValues = new HashSet<DiscreteValue>();
		
		letterValues.add(DiscreteValue.create("l0"));
		letterValues.add(DiscreteValue.create("l1"));
		
		
		DiscreteVariable letter = DiscreteVariable.create("Letter", letterValues);
	}
}
