package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class RowTest {

	@Test
	public void testSubset() {
		HashSet<DiscreteValue> letterValues = new HashSet<DiscreteValue>();
		
		letterValues.add(DiscreteValue.create("l0"));
		letterValues.add(DiscreteValue.create("l1"));
		
		HashSet<DiscreteValue> gradeValues = new HashSet<DiscreteValue>();
		gradeValues.add(DiscreteValue.create("g1"));
		gradeValues.add(DiscreteValue.create("g2"));
		gradeValues.add(DiscreteValue.create("g3"));
		
		DiscreteVariable letter = DiscreteVariable.create("Letter", letterValues);
		
		DiscreteVariable grade = DiscreteVariable.create("Grade", gradeValues);
		
		//create a joint factor
		
		HashSet<DiscreteVariable> variables = new HashSet<DiscreteVariable>();
		variables.add(letter);
		variables.add(grade);
		
		
		Row row = Row.create(0.1,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
				);
		
		assertEquals("Grade=g1	Letter=l0",row.key());
		
		variables.remove(letter);
		
		Row newRow = row.createRowFromVariableSubset(variables, true);
		
		//compare value
		assertEquals(0.1,newRow.getValue(),0.001);
		
		//check variable
		assertEquals(null,newRow.getVariableValue(letter));
		assertEquals(DiscreteVariableValue.create(grade, grade.getValueByName("g1")) ,newRow.getVariableValue(grade));
		assertEquals("Grade=g1",newRow.key());
		
	}
	
	
	@Test
	public void testProduct() {
		
		HashSet<DiscreteValue> letterValues = new HashSet<DiscreteValue>();
		
		letterValues.add(DiscreteValue.create("l0"));
		letterValues.add(DiscreteValue.create("l1"));
		
		HashSet<DiscreteValue> gradeValues = new HashSet<DiscreteValue>();
		gradeValues.add(DiscreteValue.create("g1"));
		gradeValues.add(DiscreteValue.create("g2"));
		gradeValues.add(DiscreteValue.create("g3"));
		
		DiscreteVariable letter = DiscreteVariable.create("Letter", letterValues);
		
		DiscreteVariable grade = DiscreteVariable.create("Grade", gradeValues);
		
		//create a joint factor
		
		HashSet<DiscreteVariable> variables = new HashSet<DiscreteVariable>();
		variables.add(letter);
		variables.add(grade);
		
		
		Row row1 = Row.create(0.1,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
				);
		
		Row row2 = Row.create(0.1,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
		
		Row row3 = Row.create(0.1,DiscreteVariableValue.create(letter, letter.getValueByName("l1")));
		
		
		
		//HashSet<DiscreteVariable> intersection = new HashSet<DiscreteVariable>();
		//intersection.add(letter);
		
		assertTrue(row1.compatible(row2));
		assertFalse(row1.compatible(row3));
		assertFalse(row2.compatible(row3));
		
		
		assertEquals(0.1 * 0.1, row1.product(row2, variables).getValue(), 0.0001);
		
		System.err.println(row1.product(row2, variables));
	}

}
