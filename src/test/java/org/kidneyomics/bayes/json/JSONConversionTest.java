package org.kidneyomics.bayes.json;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class JSONConversionTest {

	@Test
	public void testReadJSON() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		
		File file = new File(classLoader.getResource("student_network.json").getFile());
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JSON_TableBayesianNetwork network = gson.fromJson(new BufferedReader(new FileReader(file)), JSON_TableBayesianNetwork.class);
		
		System.err.println(gson.toJson(network));
		
		
		
		assertEquals(5,network.nodes.size());
		
		JSON_Node diff = network.nodes.get(0);
		assertEquals("Difficulty",diff.name);
		assertEquals(2,diff.cpd.columns.size());
		assertEquals(1,diff.cpd.rows.size());
		
		JSON_CPD_Row row = diff.cpd.rows.get(0);
		assertEquals(0,row.labels.size());
		assertEquals(2,row.values.size());
		assertEquals(0.6,row.values.get(0),0.001);
		assertEquals(0.4,row.values.get(1),0.001);
	}

}
