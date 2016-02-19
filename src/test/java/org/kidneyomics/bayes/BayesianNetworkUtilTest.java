package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kidneyomics.bayes.example.StudentNetwork;
import org.kidneyomics.graph.MinNeighborsEvaluationMetric;
import org.kidneyomics.graph.UndirectedNode;

public class BayesianNetworkUtilTest {

	@Test
	public void testCreateUndirectedGraph() {
		
		/*
		 * Directed
		 * 
		 * Intelligence ----> Grade
		 * Intelligence ---> SAT
		 * Difficulty ----> Grade
		 * Grade ----> Letter
		 * 
		 * 
		 */
		
		/*
		 * Undirected
		 * 
		 * Difficulty ---- Intelligence
		 * Difficulty ---- Grade
		 * Intelligence --- Grade
		 * Intelligence ---- SAT
		 * Grade ---- Letter 
		 * 
		 */
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		Map<DiscreteVariable,UndirectedNode<DiscreteVariable>> graphNodesMap  = TableBayesianNetworkUtil.createGraphNodeMapFromList(TableBayesianNetworkUtil.createUndirectedGraphFromTableNetwork(network));
		
		System.err.println("Student graph nodes");
		for(UndirectedNode<DiscreteVariable> graphNode : graphNodesMap.values()) {
			System.err.println("Variable: " + graphNode.toString());
		}
		
		UndirectedNode<DiscreteVariable> diffNode = graphNodesMap.get(diff);
		UndirectedNode<DiscreteVariable> intelNode = graphNodesMap.get(intel);
		UndirectedNode<DiscreteVariable> satNode = graphNodesMap.get(sat);
		UndirectedNode<DiscreteVariable> gradeNode = graphNodesMap.get(grade);
		UndirectedNode<DiscreteVariable> letterNode = graphNodesMap.get(letter);
		
		assertEquals(2,diffNode.neighbors().size());
		assertTrue(diffNode.neighbors().contains( intelNode));
		assertTrue(diffNode.neighbors().contains( gradeNode));
		
		assertEquals(3,intelNode.neighbors().size());
		assertTrue(intelNode.neighbors().contains( diffNode));
		assertTrue(intelNode.neighbors().contains( satNode));
		assertTrue(intelNode.neighbors().contains( gradeNode));
		
		assertEquals(3,gradeNode.neighbors().size());
		assertTrue(gradeNode.neighbors().contains( diffNode));
		assertTrue(gradeNode.neighbors().contains( intelNode));
		assertTrue(gradeNode.neighbors().contains( letterNode));
		
		
		assertEquals(1,satNode.neighbors().size());
		assertTrue(satNode.neighbors().contains( intelNode));
		
		assertEquals(1,letterNode.neighbors().size());
		assertTrue(letterNode.neighbors().contains( gradeNode));
		
	}
	
	@Test
	public void testGreedyVariableEliminationOrder() {
		
		StudentNetwork network = StudentNetwork.create();
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		System.err.println("Elimination order");
		System.err.println("--------------------------------");
		for(DiscreteVariable var : order) {
			System.err.println(var);
		}
		System.err.println("--------------------------------");
		
		assertEquals(5,order.size());
		assertTrue(network.getVariableByName("Letter").equals(order.get(0)) || network.getVariableByName("SAT").equals(order.get(0)));
		assertTrue(network.getVariableByName("Letter").equals(order.get(1)) || network.getVariableByName("SAT").equals(order.get(1)));
		assertEquals(network.getVariableByName("Difficulty"),order.get(2));
		assertTrue(network.getVariableByName("Intelligence").equals(order.get(3)) || network.getVariableByName("Grade").equals(order.get(3)));
		assertTrue(network.getVariableByName("Intelligence").equals(order.get(4)) || network.getVariableByName("Grade").equals(order.get(4)));
		
	}
	
	
	@Test
	public void testSumProductVariableElimination1() {
		System.err.println("Difficulty");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		order.remove(diff);
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.sumProductVariableElimination(network, order);
		
		System.err.println(dist);
		
		assertEquals(0.6, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.0001);
		assertEquals(0.4, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.0001);
		
	}
	
	@Test
	public void testSumProductVariableElimination2() {
		System.err.println("SAT");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable var = network.getVariableByName("SAT");
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		order.remove(var);
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.sumProductVariableElimination(network, order);
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue(),0.0001);
		
	}
	
	@Test
	public void testSumProductVariableElimination3() {
		System.err.println("Letter");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable var = network.getVariableByName("Letter");
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		order.remove(var);
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.sumProductVariableElimination(network, order);
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l1"))).getValue(),0.0001);
		
	}
	
	@Test
	public void testSumProductVariableElimination4() {
		System.err.println("Grade");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable var = network.getVariableByName("Grade");
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		order.remove(var);
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.sumProductVariableElimination(network, order);
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue();
		double exp3 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue(),0.0001);
		assertEquals(exp3, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue(),0.0001);
		
	}
	
	
	@Test
	public void testConditionalProbVarElim1() {
		System.err.println("Difficulty -- no evidence");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, diff);
		
		System.err.println(dist);
		
		assertEquals(0.6, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.0001);
		assertEquals(0.4, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.0001);
	}
	
	@Test
	public void testConditionalProbVarElim2() {
		System.err.println("Difficulty -- given grade was g1 (highest grade) ");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable grade = network.getVariableByName("Grade");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, diff, DiscreteVariableValue.create(grade, grade.getValueByName("g1")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(diff,DiscreteVariableValue.create(grade, grade.getValueByName("g1")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.0001);
	}
	
	@Test
	public void testConditionalProbVarElim3() {
		System.err.println("Difficulty -- given grade was g1 (highest grade) and student not intelligent i0 ");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, diff, DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(diff,DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.0001);
	}
	
	
	@Test
	public void testConditionalProbVarElim4() {
		System.err.println("Letter -- given grade was g1 (highest grade) and student not intelligent i0 ");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, letter, DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(letter,DiscreteVariableValue.create(grade, grade.getValueByName("g1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue(),0.0001);
	}
	
	
	@Test
	public void testConditionalProbVarElim5() {
		System.err.println("Letter -- given student not intelligent i0 ");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, letter,
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(letter,
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue(),0.0001);
	}
	
	@Test
	public void testConditionalProbVarElim6() {
		System.err.println("Letter -- given easy class was d0 and student not intelligent i0 ");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, letter, DiscreteVariableValue.create(diff, diff.getValueByName("d0")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(letter,DiscreteVariableValue.create(diff, diff.getValueByName("d0")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue();
		double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue(),0.0001);
		assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue(),0.0001);
	}
	
	
	@Test
	public void testConditionalProbVarElim7() {
		System.err.println("Letter -- given letter is l0");
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable letter = network.getVariableByName("Letter");
		
		TableProbabilityDistribution dist = TableBayesianNetworkUtil.conditionalProbVarElim(network, letter, DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
		
		System.err.println(dist);
		
		TableProbabilityDistribution longCalculationTable = network.computeProbability(letter,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
		System.err.println(longCalculationTable);
		
		double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue();
		//double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue();
		
		assertEquals(exp1, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0"))).getValue(),0.0001);
		//assertEquals(exp2, dist.getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1"))).getValue(),0.0001);
	}
	
	
	@Test
	public void testGenerateSample() {
		System.err.println("Generate Sample");
		StudentNetwork network = StudentNetwork.create();
		
		List<DiscreteVariable> order = TableBayesianNetworkUtil.topologicalSortAsDiscreteVariableList(network);
		List<DiscreteInstance> sample = TableBayesianNetworkUtil.forwardSampling(network, 100);
		
		assertEquals(100,sample.size());
		
		System.err.println(sample.get(0).titleRow(order));
		for(DiscreteInstance instance : sample) {
			assertTrue(instance.containsAllKeys(order));
			System.err.println(instance.toString(order));
		}
	}
	
	
	@Test
	public void testCalculateMaximum() {
		System.err.println("Test maximum likelihood estimation");
		
		
		StudentNetwork network = StudentNetwork.create();
		
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable sat = network.getVariableByName("SAT");
		
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable grade = network.getVariableByName("Grade");
		
		List<DiscreteInstance> sample = TableBayesianNetworkUtil.forwardSampling(network, 100000);
		
		
		TableBayesianNetworkUtil.computeMaximumLikelihoodEstimation(network, sample);
		
		
		TableNode diffNode = network.getNode(diff);
		
		
		System.err.println(diffNode.cpd());
		
		assertEquals(0.6,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d0"))).getValue(),0.01);
		assertEquals(0.4,diffNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(diff, diff.getValueByName("d1"))).getValue(),0.01);
		
		
		TableNode intelNode = network.getNode(intel);
		
		
		System.err.println(intelNode.cpd());
		
		assertEquals(0.7,intelNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).getValue(),0.01);
		assertEquals(0.3,intelNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).getValue(),0.01);
		
		
		TableNode satNode = network.getNode(sat);
		
		
		System.err.println(satNode.cpd());
		
		assertEquals(0.95,satNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(sat, sat.getValueByName("s0")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).getValue(),0.01);
		assertEquals(0.05,satNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(sat, sat.getValueByName("s1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i0"))).getValue(),0.01);
		assertEquals(0.2,satNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(sat, sat.getValueByName("s0")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).getValue(),0.01);
		assertEquals(0.8,satNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(sat, sat.getValueByName("s1")),
				DiscreteVariableValue.create(intel, intel.getValueByName("i1"))).getValue(),0.01);
		
		
		
		TableNode letterNode = network.getNode(letter);
		
		
		System.err.println(letterNode);
		
		assertEquals(0.1,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1"))).getValue(),0.01);
		assertEquals(0.9,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g1"))).getValue(),0.01);
		
		assertEquals(0.4,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g2"))).getValue(),0.01);
		assertEquals(0.6,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g2"))).getValue(),0.01);
		
		assertEquals(0.99,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l0")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g3"))).getValue(),0.01);
		assertEquals(0.01,letterNode.cpd().getFactor().getRowByValues(false, DiscreteVariableValue.create(letter, letter.getValueByName("l1")),
				DiscreteVariableValue.create(grade, grade.getValueByName("g3"))).getValue(),0.01);
		
	}

}
