package org.kidneyomics.bayes.example;

import org.junit.Test;

public class StudentNetworkTest {

	@Test
	public void testCreate() {
		
		StudentNetwork network = StudentNetwork.create();
		
		System.err.println(network.toString());
	}
	
}
