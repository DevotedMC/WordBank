package com.programmerdan.minecraft.wordbank.functions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import static com.programmerdan.minecraft.wordbank.test.WordBankTestUtil.getTestKeys;

public class TestModulusFunction extends TestFunctionBase {

	private static final Modulus function = new Modulus(wb);
	
	@Test
	public void ensureDistribution() {
		long valid = 0l;
		int min = 2;
		int max = 10;
		HashSet<Float> outcomes = new HashSet<Float>();
		ArrayList<Character[]> keys = getTestKeys();
		for (int cur = min ; cur <= max ; cur++) {
			logger.log(Level.INFO, "Testing distribution with modulo {0}", cur);
			
			Object modulus = (Object) Integer.valueOf(cur);

			float outcome;
			for (int i = 0; i < keys.size(); i++) {
				outcome = function.process(keys.get(i), modulus);
				outcomes.add(Float.valueOf(outcome));
			}
			
			logger.log(Level.INFO, "Unique outcomes with modulo {0}: {1}",
					new Object[]{modulus, outcomes.size()});
			
			valid += ((outcomes.size() == ((Integer)modulus).intValue()) ? 1l : 0l);
			
			outcomes.clear();
		}
		keys = null;
		outcomes = null;

		assertEquals((long) (max - min + 1), valid);
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
			assertEquals("The first parameter must be an Integer.", ipe.getMessage());
		}
	}

	@Test
	public void ensureParameterMagnitude() {
		try {
			function.process(getTestKeys().get(0), (Object) Integer.valueOf(0) );
		} catch (IllegalArgumentException ipe){
			assertEquals("Modulo needs to be greater then 0.", ipe.getMessage());
		}
	}
	
	@Test
	public void ensureCharacterArrayIntegrity() {
		try {
			function.process(new Character[0], (Object) Integer.valueOf(2) );
		} catch (IllegalArgumentException ipe){
			assertEquals("At least one character must be passed in.", ipe.getMessage());
		}
	}
}
