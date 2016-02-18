package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kidneyomics.bayes.CliqueTree.CliqueNode;
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
		
		tree.assignFactorsToNodes();
		
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

}
