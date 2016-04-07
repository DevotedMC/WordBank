package com.programmerdan.minecraft.wordbank.functions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import java.util.HashSet;

import static com.programmerdan.minecraft.wordbank.test.WordBankTestUtil.getTestKeys;
import com.programmerdan.minecraft.wordbank.WordBank;

public class TestModulusFunction {
	static WordBank ours = null;

	@BeforeClass
	public static void setup() {
		ours = new WordBank();
		ours.onEnable();
	}

	@AfterClass
	public static void teardown() {
		ours.onDisable();
		ours = null;
	}


	@Test
	public void ensureDistribution() {
		long valid = 0l;
		int min = 2;
		int max = 100;
		for (int cur = min ; cur <= max ; cur++) {
			valid += (ensureDistribution(cur) ? 1l : 0l);
		}

		assertEquals(valid, (long) (max - min + 1));

	}
	
	private Modulus function = new Modulus();

	private boolean ensureDistribution(int modulus) {
		int uniques = 0;
		HashSet<Float> outcomes = new HashSet<Float>();
		for (Character[] key : getTestKeys()) {
			float outcome = function.process(key, (Object) Integer.valueOf(modulus));
			outcomes.add(outcome);
		}
		return (outcomes.size() == modulus);
	}
}
