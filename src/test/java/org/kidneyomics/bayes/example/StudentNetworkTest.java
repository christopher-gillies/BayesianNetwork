package org.kidneyomics.bayes.example;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kidneyomics.bayes.DiscreteVariable;
import org.kidneyomics.bayes.DiscreteVariableValue;
import org.kidneyomics.bayes.ProbabilityDistributionUtil;
import org.kidneyomics.bayes.TableFactor;
import org.kidneyomics.bayes.TableProbabilityDistribution;

public class StudentNetworkTest {

	@Test
	public void testCreate() {
		
		StudentNetwork network = StudentNetwork.create();
		
		System.err.println(network.toString());
	}
	
	
	@Test
	public void testComputeJoint() {
		
		StudentNetwork network = StudentNetwork.create();
		
		TableFactor joint = network.computeJoint();
		
		System.err.println(joint);
		
		assertEquals(2 * 2 * 3 * 2 * 2, joint.rows().size());
		
		assertTrue(ProbabilityDistributionUtil.isNormalized(joint));
	}
	
	@Test
	public void testComputeProbability() {
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		//difficulty
		TableProbabilityDistribution res = network.computeNormalizedProbability(diff, new DiscreteVariableValue[0]);
		
		System.err.println(res);
		
		assertEquals(0.6,res.getFactor().getRowsByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).get(0).getValue(),0.0001);
		assertEquals(0.4,res.getFactor().getRowsByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).get(0).getValue(),0.0001);
		
		
		//intelligence
		TableProbabilityDistribution res2 = network.computeNormalizedProbability(intel, new DiscreteVariableValue[0]);
		
		System.err.println(res2);
		
		assertEquals(0.7,res2.getFactor().getRowsByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).get(0).getValue(),0.0001);
		assertEquals(0.3,res2.getFactor().getRowsByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).get(0).getValue(),0.0001);
		
		//letter
		TableProbabilityDistribution res3 = network.computeNormalizedProbability(letter, new DiscreteVariableValue[0]);
		
		System.err.println(res3);
		
		assertEquals(0.502,res3.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).get(0).getValue(),0.001);
		assertEquals(0.498,res3.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).get(0).getValue(),0.001);
		
		
		//letter given not smart
		TableProbabilityDistribution res4 = network.computeNormalizedProbability(letter, DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		
		System.err.println(res4);
		
		assertEquals(0.389,res4.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).get(0).getValue(),0.001);
		assertEquals(0.611,res4.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).get(0).getValue(),0.001);
		
		
		//letter given not smart and easy
		TableProbabilityDistribution res5 = network.computeNormalizedProbability(letter, DiscreteVariableValue.create(intel, intel.getValueByName("i0")), 
				DiscreteVariableValue.create(diff, diff.getValueByName("d0")));
		
		System.err.println(res5);
		
		assertEquals(0.513,res5.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).get(0).getValue(),0.001);
		assertEquals(0.487,res5.getFactor().getRowsByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).get(0).getValue(),0.001);
		
		
		
		//intelligence given a low grade
		TableProbabilityDistribution res6 = network.computeNormalizedProbability(intel, DiscreteVariableValue.create(grade, grade.getValueByName("g3")));
		
		System.err.println(res6);
		
		assertEquals(0.921,res6.getFactor().getRowsByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).get(0).getValue(),0.0001);
		assertEquals(0.079,res6.getFactor().getRowsByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).get(0).getValue(),0.0001);
		
		
		//difficulty given a low grade
		TableProbabilityDistribution res7 = network.computeNormalizedProbability(diff, DiscreteVariableValue.create(grade, grade.getValueByName("g3")));
		
		System.err.println(res7);
		
		assertEquals(0.370,res7.getFactor().getRowsByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).get(0).getValue(),0.001);
		assertEquals(0.629,res7.getFactor().getRowsByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).get(0).getValue(),0.001);
		
		
	}
}
