package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kidneyomics.bayes.example.StudentNetwork;

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
		System.err.println(dist.toString());
		
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
	
	
	
	@Test
	public void testCalculateSufficientStatistics() {
		System.err.println("testCalculateSufficientStatistics");
		
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		
		List<DiscreteInstance> sample = TableBayesianNetworkUtil.forwardSampling(network, 100000);
		
		assertEquals(100000,sample.size());
		
		TableNode diffNode = network.getNode(diff);
		
		Map<Row,Double> stats = diffNode.cpd().computeSufficientStatisticsCompleteData(sample);
		
		double sum = 0;
		for(Row row : stats.keySet()) {
			sum += stats.get(row);
		}
		
		assertEquals(100000,sum,0.0001);
		
		diffNode.cpd().maximumLikelihoodEstimation(stats);
		
		System.err.println(diffNode.cpd());
		
		assertEquals(0.6,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.01);
		assertEquals(0.4,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.01);
	}
	
	
	@Test
	public void testCalculateSufficientStatisticsMissing1() {
		System.err.println("testCalculateSufficientStatisticsMissing1");
		
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		
		List<DiscreteInstance> sample = TableBayesianNetworkUtil.forwardSampling(network, 2000);
		
		assertEquals(2000,sample.size());
		
		TableNode diffNode = network.getNode(diff);
		
		List<CliqueTree> trees = new LinkedList<CliqueTree>();
		
		for(DiscreteInstance instance : sample) {
			CliqueTree tree = CliqueTree.createFromEvidence(network, instance.evidence());
			tree.calibrateCliqueTree();
			trees.add(tree);
		}
		
		Map<Row,Double> stats = diffNode.cpd().computeSufficientStatisticsMissingData(trees);
		
		double sum = 0;
		for(Row row : stats.keySet()) {
			sum += stats.get(row);
		}
		
		assertEquals(2000,sum,0.0001);
		
		diffNode.cpd().maximumLikelihoodEstimation(stats);
		
		System.err.println(diffNode.cpd());
		
		assertEquals(0.6,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.03);
		assertEquals(0.4,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.03);
	}

	@Test
	public void testCalculateSufficientStatisticsMissing2() {
		System.err.println("testCalculateSufficientStatisticsMissing2");
		
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		
		List<DiscreteInstance> sample = TableBayesianNetworkUtil.forwardSampling(network, 2000);
		
		assertEquals(2000,sample.size());
		
		TableNode diffNode = network.getNode(diff);
		TableNode satNode = network.getNode(sat);
		
		List<CliqueTree> trees = new LinkedList<CliqueTree>();
		
		for(DiscreteInstance instance : sample) {
			instance.put(diff, DiscreteVariableValue.create(diff, DiscreteValue.createMissing()));
			instance.put(sat, DiscreteVariableValue.create(sat, DiscreteValue.createMissing()));
			System.err.println(instance.toString(network.variables()));
			CliqueTree tree = CliqueTree.createFromEvidence(network, instance.evidence());
			tree.calibrateCliqueTree();
			trees.add(tree);
		}
		
		{
			Map<Row,Double> stats = diffNode.cpd().computeSufficientStatisticsMissingData(trees);
			
			double sum = 0;
			for(Row row : stats.keySet()) {
				sum += stats.get(row);
			}
			
			assertEquals(2000,sum,0.0001);
			
			diffNode.cpd().maximumLikelihoodEstimation(stats);
			
			System.err.println(diffNode.cpd());
			
			assertEquals(0.6,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.03);
			assertEquals(0.4,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.03);
		}
		
		{
			Map<Row,Double> stats = satNode.cpd().computeSufficientStatisticsMissingData(trees);
			
			double sum = 0;
			for(Row row : stats.keySet()) {
				sum += stats.get(row);
			}
			
			assertEquals(2000,sum,0.0001);
			
			satNode.cpd().maximumLikelihoodEstimation(stats);
			
			System.err.println(satNode.cpd());
			
			assertEquals(0.95,satNode.cpd().getFactor().getRowByValues(false, 
					DiscreteVariableValue.create(sat, sat.getValueByName("s0")),
						DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).getValue(),0.03);
			
			assertEquals(0.2,satNode.cpd().getFactor().getRowByValues(false, 
					DiscreteVariableValue.create(sat, sat.getValueByName("s0")),
						DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).getValue(),0.03);
				
		}
	}
}
