package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class TableConditionalProbabilityDistributionTest {

	private TableFactor createGradeLetterFactor() {
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
		
		TableFactor factor = TableFactor.create(variables);
		
		factor.addRows(
				Row.create(10.0,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
						),
				Row.create(90.0,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
						),
				Row.create(40.0,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g2")) 
						),
				Row.create(60.0,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g2")) 
						),
				Row.create(99.0,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g3")) 
						),
				Row.create(1.0,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g3")) 
						)
					);
		return factor;
	}
	
	@Test
	public void test() {
		TableFactor table = createGradeLetterFactor();
		DiscreteVariable letter = table.getVariableByName("Letter");
		DiscreteVariable grade = table.getVariableByName("Grade");
		
		TableConditionalProbabilityDistribution dist = TableConditionalProbabilityDistribution.create(table,letter);
		
		System.err.println("CPD for Letter | Grade");
		System.err.println(table.toString());
		
		assertTrue(dist.isNormalized());

		assertEquals(0.1, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1"))
				).get(0).getValue(),0.001);
		
		assertEquals(0.9, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1"))
				).get(0).getValue(),0.001);
		
		assertEquals(0.4, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g2"))
				).get(0).getValue(),0.001);
		
		assertEquals(0.6, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g2"))
				).get(0).getValue(),0.001);
		
		assertEquals(0.99, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g3"))
				).get(0).getValue(),0.001);
		
		assertEquals(0.01, table.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g3"))
				).get(0).getValue(),0.001);
	}

}
