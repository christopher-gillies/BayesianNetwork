package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class TableDistributionTest {

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
				Row.create(0.1,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
						),
				Row.create(0.9,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g1")) 
						),
				Row.create(0.4,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g2")) 
						),
				Row.create(0.6,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g2")) 
						),
				Row.create(0.99,DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g3")) 
						),
				Row.create(0.01,DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
						DiscreteVariableValue.create(grade, grade.getValueByName("g3")) 
						)
					);
		return factor;
	}
	
	@Test
	public void test() {
		TableFactor table = createGradeLetterFactor();
		
		assertFalse(TableDistribution.isNormalized(table));
		
		TableDistribution dist = TableDistribution.create(table);
		
		assertTrue(TableDistribution.isNormalized(table));
	}

}
