package org.kidneyomics.bayes;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProbabilityDistributionTest {

	@Test
	public void testAddLogs() {
		
		double a = Math.log(0.2);
		double b = Math.log(0.5);
		
		
		double res1 = ProbabilityDistributionUtil.addLogValues(a, b);
		
		assertEquals(0.7, Math.exp(res1), 0.0001);
		
		double res2 = ProbabilityDistributionUtil.addLogValues(b, a);
		
		assertEquals(0.7, Math.exp(res2), 0.0001);
		
	}

}
