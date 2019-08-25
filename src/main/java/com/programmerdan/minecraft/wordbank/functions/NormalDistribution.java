package com.programmerdan.minecraft.wordbank.functions;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;

public class NormalDistribution extends CharFunction {

	public NormalDistribution() {
		super(null);
	}
	
	public NormalDistribution(WordBank plugin) {
		super(plugin);
	}

	/**
	 * Thin wrapper for {@link #process(Character[], String)}. 
	 * See it for details.
	 */
	public float process(Character[] input, Object...params) {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("At least one parameter was expected.");
		}
		if (params[0] instanceof String) {
			return process(input, (String) params[0]);
		} else {
			throw new IllegalArgumentException("The first parameter must be an String.");
		}
	}

	/**
	 * Filter an assumed uniform input into a normal/gaussian output, with no skew.
	 * 
	 * @param input Character sequence, first passed through HashMap function.
	 * @param param The Hashing function to use.
	 * @return a value between 0 and 1, that tends to be near 0.5 
	 */
	public float process(Character[] input, String param) {
		double q = (double) new HashMap().process(input, param);
		
		// q should be relatively uniform.
		
		//solve for x where 1=.25+(4x)-(4x)^2+(4x)^3-(4x)^4+(4x)^5
		// x = 0.898294, y = 1
		//solve for x where 0=.25+(x)-(x)^2+(x)^3-(x)^4+(x)^5
		// x = -0.200051, y = 0
		
		double x = (q * (0.898284+.200051)) - 0.200051;
		
		double outcome = Math.pow(x, 5d)-Math.pow(x, 4d)+Math.pow(x,3d)-Math.pow(x,2d)+x+0.25;
		
		return (float) (outcome < 0.0d ? 0.0f : (outcome > 1.0d ? 1.0f : outcome));
	}

}
