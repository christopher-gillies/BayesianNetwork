package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;
import org.kidneyomics.bayes.json.JSON_TableBayesianNetwork;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class TableBayesianNetworkImplTest {

	@Test
	public void testCreate() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		
		File file = new File(classLoader.getResource("student_network.json").getFile());
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JSON_TableBayesianNetwork jsonNetwork = gson.fromJson(new BufferedReader(new FileReader(file)), JSON_TableBayesianNetwork.class);
		
		System.err.println(gson.toJson(jsonNetwork));
		
		TableBayesianNetworkImpl network = TableBayesianNetworkImpl.createFromJSON(jsonNetwork);
		
		System.err.println(network.toString());
		
		
		DiscreteVariable grade = network.getVariableByName("Grade");
		DiscreteVariable diff = network.getVariableByName("Difficulty");
		DiscreteVariable intel = network.getVariableByName("Intelligence");
		DiscreteVariable letter = network.getVariableByName("Letter");
		DiscreteVariable sat = network.getVariableByName("SAT");
		
		TableNode gradeNode = network.getNode(grade);
		TableNode satNode = network.getNode(sat);
		
		TableNode diffNode = network.getNode(diff);
		
		assertEquals(0.6,diffNode.cpd().getFactor().getRowByValues(false, diff.getVariableValueByName("d0")).getValue(),0.0001);
		assertEquals(0.4,diffNode.cpd().getFactor().getRowByValues(false, diff.getVariableValueByName("d1")).getValue(),0.0001);
		assertEquals(1,diffNode.children().size());
		assertEquals(gradeNode,diffNode.children().get(0));
		
		TableNode intelNode = network.getNode(intel);
		
		assertEquals(0.7,intelNode.cpd().getFactor().getRowByValues(false, intel.getVariableValueByName("i0")).getValue(),0.0001);
		assertEquals(0.3,intelNode.cpd().getFactor().getRowByValues(false, intel.getVariableValueByName("i1")).getValue(),0.0001);
		assertEquals(2,intelNode.children().size());
		assertTrue(intelNode.children().contains(gradeNode));
		assertTrue(intelNode.children().contains(gradeNode));
		
	}

	
	@Test
	//TODO: do more tests on learning
	public void testLearning() {
		
	}
}
