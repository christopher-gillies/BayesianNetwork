package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class TableFactorTest {
	
	
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
	public void testCreate() {
		
		TableFactor factor = createGradeLetterFactor();
		
		System.err.println("Grade and Letter Factor");
		System.err.println(factor);
	
		System.err.println("Row keys");
		for(Row row : factor.rows()) {
			System.err.println(row.key());
		}
		
		assertEquals(6,factor.rows().size());
	}
	
	
	@Test
	public void testReduce() {
		TableFactor factor = createGradeLetterFactor();
		DiscreteVariable letter = factor.getVariableByName("Letter");
		DiscreteVariable grade = factor.getVariableByName("Grade");
		TableFactor letter0Factor = (TableFactor) factor.reduce(DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
		
		System.err.println("L0 table");
		System.err.println(letter0Factor);
		
		assertEquals(3, letter0Factor.rows().size());
		assertEquals(1, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g1"))).size());
		assertEquals(0.1, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g1"))).get(0).getValue(),0.0001);
		
		assertEquals(1, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g2"))).size());
		assertEquals(0.4, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g2"))).get(0).getValue(),0.0001);
		
		assertEquals(1, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g3"))).size());
		assertEquals(0.99, letter0Factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g3"))).get(0).getValue(),0.0001);
		
		TableFactor letter0Grade1Factor = (TableFactor) factor.reduce(DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1")));
		
		System.err.println("L0 G1 table");
		System.err.println(letter0Grade1Factor);
		
		assertEquals(1, letter0Grade1Factor.rows().size());
		assertEquals(0.1, letter0Grade1Factor.rows().get(0).getValue(),0.001);
		
		//Validate that changing values does not change the other tables values
		
		factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(letter, letter.getValueByName("l0"))
				).get(0).setValue(10);
		
		
		assertEquals(0.1, letter0Grade1Factor.rows().get(0).getValue(),0.001);
		assertEquals(10, factor.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(letter, letter.getValueByName("l0"))
				).get(0).getValue(),0.001);
		
	}
	
	@Test
	public void testMarginalize() {
		TableFactor factor = createGradeLetterFactor();
		DiscreteVariable letter = factor.getVariableByName("Letter");
		DiscreteVariable grade = factor.getVariableByName("Grade");
		
		/*
		 * 
		 * Grade	Letter	Value
		 *	g1	l0	0.1
		 *	g1	l1	0.9
		 *	g2	l0	0.4
		 *	g2	l1	0.6
		 *	g3	l0	0.99
		 *	g3	l1	0.01
		 *
		 *
		 */
		
		TableFactor letterMarginal = (TableFactor) factor.marinalize(grade);
		
		System.err.println("Marginalize out grade");
		System.err.println(letterMarginal.toString());
		
		assertEquals(2,letterMarginal.rows().size());
		assertEquals(1.49, letterMarginal.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).get(0).getValue(),0.001);
		assertEquals(1.51, letterMarginal.getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).get(0).getValue(),0.001);
		
		
		TableFactor gradeMarginal = (TableFactor) factor.marinalize(letter);
		
		System.err.println("Marginalize out letter");
		System.err.println(gradeMarginal.toString());
		
		assertEquals(3,gradeMarginal.rows().size());
		assertEquals(1.0, gradeMarginal.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g1"))).get(0).getValue(),0.001);
		assertEquals(1.0, gradeMarginal.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g2"))).get(0).getValue(),0.001);
		assertEquals(1.0, gradeMarginal.getRowsByValues(false, DiscreteVariableValue.create(grade, grade.getValueByName("g3"))).get(0).getValue(),0.001);
		
	}
	
	
	
	@Test
	public void testProduct() {
		HashSet<DiscreteValue> aValues = new HashSet<DiscreteValue>();
		aValues.add(DiscreteValue.create("a1"));
		aValues.add(DiscreteValue.create("a2"));
		aValues.add(DiscreteValue.create("a3"));
		
		HashSet<DiscreteValue> bValues = new HashSet<DiscreteValue>();
		bValues.add(DiscreteValue.create("b1"));
		bValues.add(DiscreteValue.create("b2"));
		
		HashSet<DiscreteValue> cValues = new HashSet<DiscreteValue>();
		cValues.add(DiscreteValue.create("c1"));
		cValues.add(DiscreteValue.create("c2"));
		
		DiscreteVariable A = DiscreteVariable.create("A", aValues);
		
		DiscreteVariable B = DiscreteVariable.create("B", bValues);
		
		DiscreteVariable C = DiscreteVariable.create("C", cValues);
		
		
		HashSet<DiscreteVariable> variablesTable1 = new HashSet<DiscreteVariable>();
		variablesTable1.add(A);
		variablesTable1.add(B);
		
		TableFactor factor1 = TableFactor.create(variablesTable1);
		
		
		HashSet<DiscreteVariable> variablesTable2 = new HashSet<DiscreteVariable>();
		variablesTable2.add(B);
		variablesTable2.add(C);
		
		
		
		factor1.addRows(
				Row.create(0.5,DiscreteVariableValue.create(A, A.getValueByName("a1")),
						DiscreteVariableValue.create(B, B.getValueByName("b1")) 
						),
				Row.create(0.8,DiscreteVariableValue.create(A, A.getValueByName("a1")),
						DiscreteVariableValue.create(B, B.getValueByName("b2")) 
						),
				Row.create(0.1,DiscreteVariableValue.create(A, A.getValueByName("a2")),
						DiscreteVariableValue.create(B, B.getValueByName("b1")) 
						),
				Row.create(0,DiscreteVariableValue.create(A, A.getValueByName("a2")),
						DiscreteVariableValue.create(B, B.getValueByName("b2")) 
						),
				Row.create(0.3,DiscreteVariableValue.create(A, A.getValueByName("a3")),
						DiscreteVariableValue.create(B, B.getValueByName("b1")) 
						),
				Row.create(0.9,DiscreteVariableValue.create(A, A.getValueByName("a3")),
						DiscreteVariableValue.create(B, B.getValueByName("b2")) 
						)
					);
		
		TableFactor factor2 = TableFactor.create(variablesTable2);
		
		factor2.addRows(
				Row.create(0.5,DiscreteVariableValue.create(B, B.getValueByName("b1")),
						DiscreteVariableValue.create(C, C.getValueByName("c1")) 
						),
				Row.create(0.7,DiscreteVariableValue.create(B, B.getValueByName("b1")),
						DiscreteVariableValue.create(C, C.getValueByName("c2")) 
						),
				Row.create(0.1,DiscreteVariableValue.create(B, B.getValueByName("b2")),
						DiscreteVariableValue.create(C, C.getValueByName("c1")) 
						),
				Row.create(0.2,DiscreteVariableValue.create(B, B.getValueByName("b2")),
						DiscreteVariableValue.create(C, C.getValueByName("c2")) 
						)


					);
		
		System.err.println("Factor (A,B)");
		System.err.println(factor1.toString());
		
		System.err.println("Factor (B,C)");
		System.err.println(factor2.toString());
		
		TableFactor prod = (TableFactor) factor1.product(factor2);
		
		System.err.println("Factor (A,B,C)");
		System.err.println(prod.toString());
		
		TableFactor prod2 = (TableFactor) factor2.product(factor1);
		
		System.err.println("Factor (A,B,C) reverse order");
		System.err.println(prod2.toString());
	}
}
