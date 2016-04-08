package com.programmerdan.minecraft.wordbank.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;



import static com.programmerdan.minecraft.wordbank.test.WordBankTestUtil.getTestKeys;

/**
 * Tests the distribution of the results of hashing functions against NormalDistribution function
 * 
 * @author ProgrammerDan
 *
 */
public class TestNormalDistributionFunction extends TestFunctionBase {
	
	protected static final NormalDistribution function = new NormalDistribution(wb);
	
	/**
	 * This is a distribution test. We estimate the distribution by
	 * sorting everything into buckets, then testing if those buckets are largely uniform.
	 */
	@Test
	public void ensureMD5NormalDistribution() {
		ensureNormalDistribution("MD5");
	}
	
	@Test
	public void ensureSHA1NormalDistribution() {
		ensureNormalDistribution("SHA-1");
	}
	
	@Test
	public void ensureSHA256NormalDistribution() {
		ensureNormalDistribution("SHA-256");
	}

	@Test
	public void ensureSHA512NormalDistribution() {
		ensureNormalDistribution("SHA-512");
	}
	
	public void ensureNormalDistribution(String hash) {
		ArrayList<Character[]> keys = getTestKeys();

		int uniformBucketSize = 500;
		int multiplier = keys.size() / uniformBucketSize;
		int[] buckets = new int[multiplier];
		int[] normals = new int[multiplier];
		Random compare = new Random();
		
		// We're only pseudo-normal.
		double maxAverageVariance = 150f;

		log(Level.INFO, "Testing distribution with hash {0} and uniform bucket size {1}", 
				hash, uniformBucketSize);
			
		float outcome;
		float boxer;
		int boxed;
		for (int i = 0; i < keys.size(); i++) {
			outcome = function.process(keys.get(i), hash);
			boxed = (int) (multiplier * outcome);
			buckets[ (boxed < 0 ? 0 : boxed >= multiplier ? (int) multiplier-1 : boxed) ] ++;
			boxer = (float) (0.5d + (compare.nextGaussian() / 5d));
			boxed = (int) (multiplier * boxer);
			normals[ (boxed < 0 ? 0 : boxed >= multiplier ? (int) multiplier-1 : boxed) ] ++;
		}
		
		// Measure variance
		int peakVariance = 0;
		double varianceAvg = 0d;
		for (int i = 0; i < multiplier; i++ ) {
			int localVariance = Math.abs(normals[i] - buckets[i]);
			if (localVariance > peakVariance) peakVariance = localVariance;
			varianceAvg += localVariance;
			// display woo
			//System.out.println(String.format("%3d: h%4d g%4d", i, buckets[i], normals[i]));
		}
		varianceAvg /= buckets.length;
		
		keys = null;
		buckets = null;

		log(Level.INFO, "Max Variance Tolerance: {0} - Peak Observed Tolerance: {1} - Avg Observed Tolerance: {2}",
				maxAverageVariance, peakVariance, varianceAvg);
		
		assertTrue("Observed variance exceeds max tolerance off normal by " + (varianceAvg - maxAverageVariance), maxAverageVariance >= varianceAvg);
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
