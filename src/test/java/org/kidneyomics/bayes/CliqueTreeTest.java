package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kidneyomics.bayes.CliqueTree.CliqueNode;
import org.kidneyomics.bayes.example.StudentNetwork;
import org.kidneyomics.bayes.example.StudentNetworkExtended;

public class CliqueTreeTest {

	//Table 9.1 order
	@Test
	public void test() {
		System.err.println("Full tree");
		StudentNetworkExtended network = StudentNetworkExtended.create();
		List<DiscreteVariable> elimOrder = new LinkedList<DiscreteVariable>();
		
		DiscreteVariable cohere = network.getVariableByName("Coherence");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable job = network.getVariableByName("Job");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable happy = network.getVariableByName("Happy");
		
		
		elimOrder.add(cohere);
		elimOrder.add(diff);
		elimOrder.add(intel);
		elimOrder.add(happy);
		elimOrder.add(grade);
		elimOrder.add(sat);
		elimOrder.add(letter);
		
		
		
		CliqueTree tree = CliqueTree.createFromNetworkAndOrder(network, elimOrder);
		System.err.println(tree.toString());
		
		List<CliqueNode> nodes = tree.nodesList();
		
		assertEquals(7,nodes.size());
		
		//0
		{
			CliqueNode node = nodes.get(0);
			assertTrue(node.scopeContains(cohere,diff));
			assertEquals(1,node.neighbors().size());
			
			CliqueNode neighbor = node.neighbors().get(0);
			assertEquals(nodes.get(1),neighbor);
			assertTrue(node.sepSet(neighbor).setContains(diff));
		}
		
		//1
		{
			CliqueNode node = nodes.get(1);
			assertTrue(node.scopeContains(intel,diff,grade));
			
			
			assertEquals(2,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(0)));
			assertTrue(node.hasNeighbor(nodes.get(2)));
			
			assertTrue(node.sepSet( nodes.get(2) ).setContains(grade,intel)               );
		}
		
		
		// 6 
		{
			CliqueNode node = nodes.get(6);
			assertTrue(node.scopeContains(job,letter));
			
			
			assertEquals(1,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(5)));
			
			assertTrue(node.sepSet( nodes.get(5) ).setContains(job,letter)               );
		}
	}
	
	
	@Test
	public void testPrune() {
		StudentNetworkExtended network = StudentNetworkExtended.create();
		List<DiscreteVariable> elimOrder = new LinkedList<DiscreteVariable>();
		
		DiscreteVariable cohere = network.getVariableByName("Coherence");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable job = network.getVariableByName("Job");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable happy = network.getVariableByName("Happy");
		
		
		elimOrder.add(cohere);
		elimOrder.add(diff);
		elimOrder.add(intel);
		elimOrder.add(happy);
		elimOrder.add(grade);
		elimOrder.add(sat);
		elimOrder.add(letter);
		
		
		
		CliqueTree tree = CliqueTree.createFromNetworkAndOrder(network, elimOrder);
		
		List<CliqueNode> nodes = tree.nodesList();
		
		assertEquals(7,tree.nodes().size());
		
		tree.pruneNode(nodes.get(5));
		
		assertEquals(6,tree.nodes().size());
		
		System.err.println("Pruned tree");
		System.err.println(tree.toString());
		
		
		List<CliqueNode> nodesNew = tree.nodesList();
		
		// 4
		{
			CliqueNode node = nodesNew.get(4);
			assertTrue(node.scopeContains(grade,job,sat,letter));
			
			
			assertEquals(3,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(6)));
			
			assertTrue(node.sepSet( nodes.get(6) ).setContains(job,letter)               );
		}
		
		
		// 6
		{
			CliqueNode node = nodesNew.get(5);
			assertTrue(node.scopeContains(job,letter));
			
			
			assertEquals(1,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(4)));
			
			assertTrue(node.sepSet( nodes.get(4) ).setContains(job,letter)               );
		}
	}
	
	
	@Test
	public void testPruneNonmaximal() {
		StudentNetworkExtended network = StudentNetworkExtended.create();
		List<DiscreteVariable> elimOrder = new LinkedList<DiscreteVariable>();
		
		DiscreteVariable cohere = network.getVariableByName("Coherence");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable job = network.getVariableByName("Job");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable happy = network.getVariableByName("Happy");
		
		
		elimOrder.add(cohere);
		elimOrder.add(diff);
		elimOrder.add(intel);
		elimOrder.add(happy);
		elimOrder.add(grade);
		elimOrder.add(sat);
		elimOrder.add(letter);
		
		
		
		CliqueTree tree = CliqueTree.createFromNetworkAndOrder(network, elimOrder);
		
		List<CliqueNode> nodes = tree.nodesList();
		
		assertEquals(7,tree.nodes().size());
		
		tree.pruneNonmaximalNodes();
		
		assertEquals(5,tree.nodes().size());
		
		System.err.println("Pruned non-maximal tree");
		System.err.println(tree.toString());
		
		List<CliqueNode> nodesNew = tree.nodesList();
		
		// 4
		{
			CliqueNode node = nodes.get(4);
			assertTrue(node.scopeContains(grade,job,sat,letter));
			
			
			assertEquals(2,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(2)));
			assertTrue(node.hasNeighbor(nodes.get(3)));
			
			//sep sets
			assertTrue(node.sepSet( nodes.get(2) ).setContains(grade,sat) );
			
			assertTrue(node.sepSet( nodes.get(3) ).setContains(grade,job) );
		}
		
		

	}
	
	
	@Test
	public void testAssignFactors() {
		StudentNetworkExtended network = StudentNetworkExtended.create();
		List<DiscreteVariable> elimOrder = new LinkedList<DiscreteVariable>();
		
		DiscreteVariable cohere = network.getVariableByName("Coherence");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable job = network.getVariableByName("Job");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable happy = network.getVariableByName("Happy");
		
		
		elimOrder.add(cohere);
		elimOrder.add(diff);
		elimOrder.add(intel);
		elimOrder.add(happy);
		elimOrder.add(grade);
		elimOrder.add(sat);
		elimOrder.add(letter);
		
		
		
		CliqueTree tree = CliqueTree.createFromNetworkAndOrder(network, elimOrder);
		
		List<CliqueNode> nodes = tree.nodesList();
		
		assertEquals(7,tree.nodes().size());
		
		tree.pruneNonmaximalNodes();
		
		assertEquals(5,tree.nodes().size());
		
		tree.assignFactorsToNodesAndInitialize();
		
		System.err.println("Tree with Factors assigned");
		System.err.println(tree.toString());
		
		
		// 4
		{
			CliqueNode node = nodes.get(4);
			assertTrue(node.scopeContains(grade,job,sat,letter));
			
			assertEquals(2,node.factors().size());
			
			assertTrue(node.factors().contains(network.getNode(letter).factor()));
			assertTrue(node.factors().contains(network.getNode(job).factor()));
			
			assertEquals(2,node.neighbors().size());
			assertTrue(node.hasNeighbor(nodes.get(2)));
			assertTrue(node.hasNeighbor(nodes.get(3)));
			
			//sep sets
			assertTrue(node.sepSet( nodes.get(2) ).setContains(grade,sat) );
			
			assertTrue(node.sepSet( nodes.get(3) ).setContains(grade,job) );
		}
		
	}
	
	
	@Test
	public void testCalibrateCliqueTreeMarginals() {
		System.err.println("testCalibrateCliqueTree Marginal");
		StudentNetwork network = StudentNetwork.create();

		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		

		
		
		CliqueTree tree = CliqueTree.create(network);
		
		tree.calibrateCliqueTree();
		
		{
			DiscreteVariable var = diff;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for" + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d1"))).getValue(),0.001);
		}
		
		
		{
			DiscreteVariable var = sat;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = intel;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i1"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = letter;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l1"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = grade;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var);
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue();
			double exp3 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue(),0.001);
			assertEquals(exp3, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue(),0.001);
		}
	}

	@Test
	public void testCalibrateCliqueTreeEvidence() {
		System.err.println("testCalibrateCliqueTree with Evidence");
		StudentNetwork network = StudentNetwork.create();

		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		

		
		
		CliqueTree tree = CliqueTree.create(network,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
		
		tree.calibrateCliqueTree();
		
		{
			DiscreteVariable var = diff;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
			
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("d1"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = intel;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
			
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("i1"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = letter;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
			
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("l0"))).getValue(),0.001);
		}
		
		
		{
			DiscreteVariable var = grade;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
			
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue();
			double exp3 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g1"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g2"))).getValue(),0.001);
			assertEquals(exp3, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("g3"))).getValue(),0.001);
		}
		
		{
			DiscreteVariable var = sat;
			TableProbabilityDistribution marge = tree.marginalProbability(var);
			
			System.err.println("marginal for " + var);
			System.err.println(marge.toString());
			
			TableProbabilityDistribution longCalculationTable = network.computeProbability(var,DiscreteVariableValue.create(letter, letter.getValueByName("l0")));
			
			System.err.println("long for " + var);
			System.err.println(longCalculationTable.toString());
			
			double exp1 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue();
			double exp2 = longCalculationTable.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue();
			
			assertEquals(exp1, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s0"))).getValue(),0.001);
			assertEquals(exp2, marge.getFactor().getRowByValues(false, DiscreteVariableValue.create(var, var.getValueByName("s1"))).getValue(),0.001);
		}
	}
	
	@Test
	public void testValidateCalibration() {
		System.err.println("testValidateCalibration");
		
		StudentNetwork network = StudentNetwork.create();

		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable sat = network.getVariableByName("SAT");
		DiscreteVariable letter = network.getVariableByName("Letter");
		

		
		
		CliqueTree tree = CliqueTree.create(network);
		
		tree.calibrateCliqueTree();
		assertTrue(tree.validateCalibration());
		
	}
}
