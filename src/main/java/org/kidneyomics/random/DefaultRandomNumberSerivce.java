package org.kidneyomics.random;

import java.util.Map;
import java.util.Random;

public class DefaultRandomNumberSerivce implements RandomNumberService {

	private final Random random;
	private static final double epsilon = 0.000001;
	
	public DefaultRandomNumberSerivce(long seed) {
		random = new Random(seed);
	}
	
	public DefaultRandomNumberSerivce() {
		random = new Random();
	}
	
	public double getNextRandomNumber() {
		return random.nextDouble();
	}


	public <T> T emit(Map<T,Double> map) {
		assert(sumsToOne(map));
		//based on the probabilities of each symbol/sate emit a symbol/state
		double current = 0;
		double rand = getNextRandomNumber();
		
		T ret = null;
		
		//sum through the entries
		// 0                     0.5              0.75			1.0
		// |--------entry1--------||----entry1----||----entry3----|
		//Each entry is assigned a specific range between 0 and 1 based on its probability
		//Generate a uniform random number U between 0 and 1 and see what range it ends up with
		//since U is uniform the probability that it ends in entry one equals the area entry 1 covers and so on for other entries
		for(Map.Entry<T, Double> entry : map.entrySet()) {
			double val = entry.getValue();
			current += val;
			if(rand <= current) {
				ret = entry.getKey();
				break;
			}
		}
		
		return ret;
	}
	
	
	public <T> boolean sumsToOne(Map<T, Double> map) {
		return(sumsToOne(map,epsilon));
	}
	
	public <T> boolean sumsToOne(Map<T,Double> map, double epsilon) {
		double sum = 0;
		for(Map.Entry<T, Double> entry : map.entrySet()) {
			double val = entry.getValue();
			sum += val;
		}
		
		return(Math.abs(sum - 1.0) < epsilon);
	}
}
