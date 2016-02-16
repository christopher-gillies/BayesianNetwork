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
		
		Map<DiscreteVariable,UndirectedNode<DiscreteVariable>> graphNodesMap  = BayesianNetworkUtil.createGraphNodeMapFromList(BayesianNetworkUtil.createUndirectedGraphFromTableNodes(network.nodes()));
		
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
		List<UndirectedNode<DiscreteVariable>> nodes = BayesianNetworkUtil.createUndirectedGraphFromTableNodes(network.nodes());
		
		List<DiscreteVariable> order = BayesianNetworkUtil.greedyVariableEliminationOrder(nodes, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
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

}
