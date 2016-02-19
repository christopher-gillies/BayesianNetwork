package org.kidneyomics.random;

import java.util.Map;

public interface RandomNumberService {
	double getNextRandomNumber();
	
	/**
	 * 
	 * @param map whose values sum to 1
	 * @return a random T based on the probability of its value
	 */
	<T> T emit(Map<T,Double> map);

	<T> boolean sumsToOne(Map<T, Double> map);
}
