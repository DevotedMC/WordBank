package com.programmerdan.minecraft.wordbank.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.logging.Level;


import static com.programmerdan.minecraft.wordbank.test.WordBankTestUtil.getTestKeys;

/**
 * Tests the uniformity of the results of hashing functions against HashMap function
 * 
 * @author ProgrammerDan
 *
 */
public class TestHashMapFunction extends TestFunctionBase {
	
	protected static final HashMap function = new HashMap(wb);
	
	/**
	 * This is a distribution test. We estimate the distribution by
	 * sorting everything into buckets, then testing if those buckets are largely uniform.
	 */
	@Test
	public void ensureMD5UniformDistribution() {
		ensureDistribution("MD5");
	}
	
	@Test
	public void ensureSH1UniformDistribution() {
		ensureDistribution("SHA-1");
	}
	
	@Test
	public void ensureSHA256UniformDistribution() {
		ensureDistribution("SHA-256");
	}

	@Test
	public void ensureSHA512UniformDistribution() {
		ensureDistribution("SHA-512");
	}
	
	public void ensureDistribution(String hash) {
		ArrayList<Character[]> keys = getTestKeys();

		int expectedBucketSize = 500;
		int multiplier = keys.size() / expectedBucketSize;
		int[] buckets = new int[multiplier];
		
		double maxAverageVariance = 20f;

		log(Level.INFO, "Testing distribution with hash {0} and expected bucket size {1}", 
				hash, expectedBucketSize);
			
		float outcome;
		for (int i = 0; i < keys.size(); i++) {
			outcome = function.process(keys.get(i), hash);
			buckets[ (int) (multiplier * outcome) ] ++;
		}
		
		// Measure variance
		int peakVariance = 0;
		double varianceAvg = 0d;
		for (int bucket : buckets ) {
			int localVariance = Math.abs(expectedBucketSize - bucket);
			if (localVariance > peakVariance) peakVariance = localVariance;
			varianceAvg += localVariance;
		}
		varianceAvg /= buckets.length;
		
		keys = null;
		buckets = null;

		log(Level.INFO, "Max Variance Tolerance: {0} - Peak Observed Tolerance: {1} - Avg Observed Tolerance: {2}",
				maxAverageVariance, peakVariance, varianceAvg);
		
		assertTrue("Observed variance exceeds max tolerance off uniform by " + (varianceAvg - maxAverageVariance), maxAverageVariance >= varianceAvg);
	}

	@Test
	public void ensureParameterExists() {
		try {
			function.process(getTestKeys().get(0));
		} catch (IllegalArgumentException ipe){
			assertEquals("At least one parameter was expected.", ipe.getMessage());
		}
	}

	@Test
	public void ensureParameterType() {
		try {
			function.process(getTestKeys().get(0), new Object() );
		} catch (IllegalArgumentException ipe){
			assertEquals("The first parameter must be an String.", ipe.getMessage());
		}
	}

	@Test
	public void ensureHashingLimitations() {
		try {
			function.process(getTestKeys().get(0), (Object) new String("blowfish"));
		} catch (IllegalArgumentException ipe){
			assertEquals("Failed to instantiate cryptographic function blowfish", ipe.getMessage());
		}
	}
	
	@Test
	public void ensureCharacterArrayIntegrity() {
		try {
			function.process(new Character[0], (Object) new String("MD5") );
		} catch (IllegalArgumentException ipe){
			assertEquals("At least one character must be passed in.", ipe.getMessage());
		}
	}
}
